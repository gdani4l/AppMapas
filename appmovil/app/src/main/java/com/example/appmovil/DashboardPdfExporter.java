package com.example.appmovil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Genera reportes PDF sin depender de servicios externos ni permisos de almacenamiento. */
final class DashboardPdfExporter {
    private DashboardPdfExporter() { }

    static void exportSummary(Context context, Uri destination,
                              JSONObject summary, JSONObject usage) throws Exception {
        try (Writer writer = new Writer(context)) {
            writer.title("Resumen de la plataforma");
            writer.paragraph("Generado el " + generatedAt());
            writer.note("Los recorridos de prueba se integran en las métricas generales. "
                    + "Las zonas representan muestras de ubicación agrupadas, no visitas verificadas a establecimientos.");

            writer.section("Indicadores de contenido");
            String[] contentLabels={"Lugares","Reseñas","Zonas de riesgo","Pendientes"};
            long[] contentValues={summary.optLong("lugares"),summary.optLong("resenas"),
                    summary.optLong("reportesSeguridad"),summary.optLong("denunciasPendientes")};
            writer.metricCards(contentLabels,contentValues);
            writer.donutChart("Distribución del contenido",series(contentLabels,contentValues));

            writer.section("Usuarios y movilidad");
            long allPoints = usage.optLong("puntos");
            JSONArray zones=usage.optJSONArray("zonas");
            writer.metricCards(new String[]{"Usuarios","Activos 7 días","Zonas","Muestras"},
                    new long[]{usage.optLong("usuarios"),usage.optLong("activos7Dias"),
                            zones==null?0:zones.length(),allPoints});

            JSONArray recent = usage.optJSONArray("ultimosDias");
            if (recent == null || recent.length() == 0) {
                writer.section("Actividad reciente");
                writer.paragraph("Todavía no hay actividad registrada durante los últimos siete días.");
            } else {
                String[] labels=new String[recent.length()];long[] users=new long[recent.length()];
                long[] points=new long[recent.length()];
                for (int i = 0; i < recent.length(); i++) {
                    JSONObject day = recent.optJSONObject(i);
                    labels[i]=day==null?"":day.optString("fecha");
                    users[i]=day==null?0:day.optLong("usuarios");
                    points[i]=day==null?0:day.optLong("puntos");
                }
                writer.lineChart("Usuarios activos por día",series(labels,users));
                writer.lineChart("Muestras de recorrido por día",series(labels,points));
            }
            writer.write(context, destination);
        }
    }

    static void exportCommunity(Context context, Uri destination, JSONObject data) throws Exception {
        try (Writer writer = new Writer(context)) {
            writer.title("Análisis de comunidad");
            writer.paragraph("Generado el " + generatedAt());
            writer.note("Los totales son históricos, salvo las altas indicadas para los últimos 30 días. "
                    + "Los recorridos de prueba se integran en las métricas generales.");

            writer.lineChart("Nuevos usuarios · últimos 30 días", data.optJSONArray("registros"));
            writer.dataset("Usuarios por distrito declarado", data.optJSONArray("distritos"));
            writer.barChart("Distribución de valoraciones", data.optJSONArray("valoraciones"));
            writer.dataset("Incidentes por tipo", data.optJSONArray("incidentes"));
            writer.dataset("Reportes de seguridad por distrito", data.optJSONArray("reportesDistritos"));
            writer.lineChart("Horarios con más reportes", data.optJSONArray("reportesHoras"));
            writer.barChart("Días con más reportes", data.optJSONArray("reportesDias"));
            writer.dataset("Incidente predominante en cada distrito", data.optJSONArray("incidentesDistrito"));
            writer.donutChart("Reportes confirmados, refutados y pendientes", data.optJSONArray("validacion"));
            writer.dataset("Alertas recibidas por distrito", data.optJSONArray("zonasAlertas"));
            writer.dataset("Reportes en zonas reincidentes por distrito", data.optJSONArray("zonasReincidentes"));
            writer.dataset("Distritos con mayor actividad", data.optJSONArray("actividadDistritos"));

            writer.write(context, destination);
        }
    }

    private static String generatedAt() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
    }

    private static JSONArray series(String[] labels,long[] values) throws Exception {
        JSONArray result=new JSONArray();
        for(int i=0;i<labels.length&&i<values.length;i++)
            result.put(new JSONObject().put("etiqueta",labels[i]).put("valor",values[i]));
        return result;
    }

    private static final class Writer implements AutoCloseable {
        private static final int WIDTH = 595;
        private static final int HEIGHT = 842;
        private static final float MARGIN = 42f;
        private static final float CONTENT_WIDTH = WIDTH - MARGIN * 2;

        private final PdfDocument document = new PdfDocument();
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int primary;
        private final int secondary;
        private final int text;
        private PdfDocument.Page page;
        private Canvas canvas;
        private float y;
        private int pageNumber;

        Writer(Context context) {
            primary = context.getColor(R.color.app_primary_dark);
            secondary = context.getColor(R.color.app_secondary);
            text = context.getColor(R.color.app_text_primary);
            newPage();
        }

        void title(String value) {
            ensure(60);
            paint.setColor(primary);
            paint.setTextSize(25);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText(value, MARGIN, y, paint);
            y += 35;
            paint.setColor(secondary);
            canvas.drawRect(MARGIN, y, MARGIN + 110, y + 4, paint);
            y += 20;
        }

        void section(String value) {
            ensure(42);
            y += 10;
            paint.setColor(primary);
            paint.setTextSize(17);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText(value, MARGIN, y, paint);
            y += 23;
        }

        void paragraph(String value) { wrapped(value, 11.5f, Color.rgb(71, 85, 105), false, 16f); }

        void note(String value) {
            ensure(68);
            paint.setColor(Color.rgb(224, 242, 254));
            canvas.drawRoundRect(MARGIN, y - 5, WIDTH - MARGIN, y + 53, 8, 8, paint);
            float old = y;
            y += 10;
            wrapped(value, 10.5f, Color.rgb(3, 105, 161), false, 14f);
            y = Math.max(y + 5, old + 62);
        }

        void metric(String label, long value) { row(label, String.valueOf(value)); }

        void metricCards(String[] labels,long[] values) {
            float gap=12f,cardWidth=(CONTENT_WIDTH-gap)/2f;
            for(int i=0;i<labels.length&&i<values.length;i+=2){
                ensure(76);float rowTop=y-6;
                for(int column=0;column<2&&i+column<labels.length&&i+column<values.length;column++){
                    int index=i+column;float left=MARGIN+column*(cardWidth+gap);
                    paint.setColor(column==0?Color.rgb(224,242,254):Color.rgb(204,251,241));
                    canvas.drawRoundRect(left,rowTop,left+cardWidth,rowTop+62,9,9,paint);
                    paint.setColor(text);paint.setTextSize(20);paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
                    canvas.drawText(String.valueOf(values[index]),left+14,rowTop+28,paint);
                    paint.setColor(Color.rgb(71,85,105));paint.setTextSize(10.5f);paint.setTypeface(Typeface.DEFAULT);
                    canvas.drawText(trimToWidth(labels[index],cardWidth-28),left+14,rowTop+49,paint);
                }
                y=rowTop+75;
            }
        }

        void row(String label, String value) {
            ensure(29);
            paint.setTextSize(11);
            paint.setTypeface(Typeface.DEFAULT);
            paint.setColor(text);
            String safeLabel = trimToWidth(label, CONTENT_WIDTH * 0.62f);
            canvas.drawText(safeLabel, MARGIN, y, paint);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            paint.setColor(primary);
            float valueWidth = paint.measureText(value);
            canvas.drawText(value, Math.max(MARGIN + CONTENT_WIDTH * 0.64f,
                    WIDTH - MARGIN - valueWidth), y, paint);
            paint.setColor(Color.rgb(226, 232, 240));
            canvas.drawLine(MARGIN, y + 8, WIDTH - MARGIN, y + 8, paint);
            y += 25;
        }

        void lineChart(String title,JSONArray values){
            if(values==null||values.length()==0){section(title);paragraph("Todavía no hay datos para este indicador.");return;}
            ensure(245);section(title);
            float left=MARGIN+32,right=WIDTH-MARGIN-8,top=y+8,bottom=top+145;
            paint.setColor(Color.rgb(248,250,252));canvas.drawRoundRect(MARGIN,y,right+8,bottom+34,10,10,paint);
            long max=maximum(values);
            paint.setStrokeWidth(1);paint.setColor(Color.rgb(226,232,240));
            for(int i=0;i<=4;i++){float gy=bottom-(bottom-top)*i/4f;canvas.drawLine(left,gy,right,gy,paint);
                paint.setTextSize(8.5f);paint.setColor(Color.rgb(100,116,139));canvas.drawText(String.valueOf(Math.round(max*i/4f)),MARGIN+3,gy+3,paint);paint.setColor(Color.rgb(226,232,240));}
            Path path=new Path();float step=values.length()<=1?0:(right-left)/(values.length()-1f);
            for(int i=0;i<values.length();i++){JSONObject item=values.optJSONObject(i);long value=item==null?0:item.optLong("valor");
                float x=left+step*i,py=bottom-(bottom-top)*value/(float)max;if(i==0)path.moveTo(x,py);else path.lineTo(x,py);}
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(3);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);paint.setColor(primary);canvas.drawPath(path,paint);
            paint.setStyle(Paint.Style.FILL);
            for(int i=0;i<values.length();i++){if(!showChartLabel(i,values.length()))continue;JSONObject item=values.optJSONObject(i);long value=item==null?0:item.optLong("valor");
                float x=left+step*i,py=bottom-(bottom-top)*value/(float)max;paint.setColor(Color.WHITE);canvas.drawCircle(x,py,4.5f,paint);paint.setColor(primary);canvas.drawCircle(x,py,2.7f,paint);
                paint.setTextSize(8);paint.setColor(Color.rgb(71,85,105));String label=chartLabel(item==null?"":item.optString("etiqueta"));
                float tx=Math.max(MARGIN,Math.min(x-paint.measureText(label)/2,right-paint.measureText(label)));canvas.drawText(label,tx,bottom+18,paint);}
            y=bottom+43;
        }

        void barChart(String title,JSONArray values){
            if(values==null||values.length()==0){section(title);paragraph("Todavía no hay datos para este indicador.");return;}
            ensure(235);section(title);
            float left=MARGIN+30,right=WIDTH-MARGIN-8,top=y+8,bottom=top+135;long max=maximum(values);
            paint.setColor(Color.rgb(248,250,252));canvas.drawRoundRect(MARGIN,y,right+8,bottom+34,10,10,paint);
            float slot=(right-left)/Math.max(1,values.length()),bar=Math.min(34,slot*.62f);
            for(int i=0;i<values.length();i++){JSONObject item=values.optJSONObject(i);long value=item==null?0:item.optLong("valor");
                float center=left+slot*(i+.5f),height=(bottom-top)*value/(float)max;
                paint.setColor(chartColor(i));canvas.drawRoundRect(center-bar/2,bottom-height,center+bar/2,bottom,5,5,paint);
                paint.setTextSize(9);paint.setColor(text);String number=String.valueOf(value);canvas.drawText(number,center-paint.measureText(number)/2,bottom-height-5,paint);
                paint.setTextSize(8);paint.setColor(Color.rgb(71,85,105));String label=trimLabel(chartLabel(item==null?"":item.optString("etiqueta")),10);
                canvas.drawText(label,center-paint.measureText(label)/2,bottom+17,paint);}
            y=bottom+43;
        }

        void donutChart(String title,JSONArray values){
            if(values==null||values.length()==0){section(title);paragraph("Todavía no hay datos para este indicador.");return;}
            int rows=Math.max(2,values.length());float needed=Math.max(190,rows*28+55);ensure(needed+45);section(title);
            float top=y+4,height=needed-35,cx=MARGIN+92,cy=top+height/2,radius=57;long total=0;
            for(int i=0;i<values.length();i++){JSONObject item=values.optJSONObject(i);if(item!=null)total+=Math.max(0,item.optLong("valor"));}
            paint.setColor(Color.rgb(248,250,252));canvas.drawRoundRect(MARGIN,y,WIDTH-MARGIN,y+needed,10,10,paint);
            RectF oval=new RectF(cx-radius,cy-radius,cx+radius,cy+radius);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(22);paint.setStrokeCap(Paint.Cap.BUTT);
            float angle=-90;
            if(total==0){paint.setColor(Color.rgb(226,232,240));canvas.drawArc(oval,0,360,false,paint);}else for(int i=0;i<values.length();i++){
                JSONObject item=values.optJSONObject(i);long value=item==null?0:Math.max(0,item.optLong("valor"));float sweep=360f*value/total;
                paint.setColor(chartColor(i));canvas.drawArc(oval,angle,Math.max(1,sweep-1.5f),false,paint);angle+=sweep;}
            paint.setStyle(Paint.Style.FILL);paint.setColor(text);paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));paint.setTextSize(19);
            String totalText=String.valueOf(total);canvas.drawText(totalText,cx-paint.measureText(totalText)/2,cy+6,paint);
            float lx=MARGIN+185,ly=top+13;
            for(int i=0;i<values.length();i++){JSONObject item=values.optJSONObject(i);String label=chartLabel(item==null?"":item.optString("etiqueta"));long value=item==null?0:item.optLong("valor");
                paint.setColor(chartColor(i));canvas.drawCircle(lx,ly,5,paint);paint.setColor(text);paint.setTextSize(10.5f);paint.setTypeface(Typeface.DEFAULT);
                canvas.drawText(trimLabel(label,24)+"  "+value,lx+13,ly+4,paint);ly+=27;}
            y+=needed+8;
        }

        private long maximum(JSONArray values){long max=1;for(int i=0;i<values.length();i++){JSONObject item=values.optJSONObject(i);if(item!=null)max=Math.max(max,item.optLong("valor"));}return max;}
        private boolean showChartLabel(int index,int count){if(count<=7)return true;int interval=(int)Math.ceil(count/5d);return index==0||index==count-1||index%interval==0;}
        private String chartLabel(String value){if(value==null)return "";String clean=value.replace('_',' ');if(clean.length()>=10&&clean.charAt(4)=='-'&&clean.charAt(7)=='-')return clean.substring(8,10)+"/"+clean.substring(5,7);return clean;}
        private String trimLabel(String value,int max){return value.length()<=max?value:value.substring(0,Math.max(1,max-1))+"…";}
        private int chartColor(int index){int[] colors={primary,secondary,Color.rgb(245,158,11),Color.rgb(239,68,68),Color.rgb(139,92,246),Color.rgb(20,184,166)};return colors[index%colors.length];}

        void dataset(String title, JSONArray values) {
            section(title);
            if (values == null || values.length() == 0) {
                paragraph("Todavía no hay datos para este indicador.");
                return;
            }
            long maximum = 1;
            for (int i = 0; i < values.length(); i++) {
                JSONObject item = values.optJSONObject(i);
                if (item != null) maximum = Math.max(maximum, item.optLong("valor"));
            }
            for (int i = 0; i < values.length(); i++) {
                JSONObject item = values.optJSONObject(i);
                if (item == null) continue;
                ensure(36);
                String label = item.optString("etiqueta").replace('_', ' ');
                long value = item.optLong("valor");
                paint.setTextSize(10.5f);
                paint.setTypeface(Typeface.DEFAULT);
                paint.setColor(text);
                canvas.drawText(trimToWidth(label, CONTENT_WIDTH - 50), MARGIN, y, paint);
                String number = String.valueOf(value);
                canvas.drawText(number, WIDTH - MARGIN - paint.measureText(number), y, paint);
                y += 8;
                paint.setColor(Color.rgb(226, 232, 240));
                canvas.drawRoundRect(MARGIN, y, WIDTH - MARGIN, y + 8, 4, 4, paint);
                paint.setColor(secondary);
                float barWidth = Math.max(3, CONTENT_WIDTH * value / (float) maximum);
                canvas.drawRoundRect(MARGIN, y, MARGIN + barWidth, y + 8, 4, 4, paint);
                y += 23;
            }
        }

        void user(String name, String contributions, String movement) {
            ensure(58);
            wrapped(name, 11.5f, text, true, 15f);
            wrapped(contributions, 10.3f, Color.rgb(71, 85, 105), false, 14f);
            wrapped(movement, 10.3f, Color.rgb(71, 85, 105), false, 14f);
            paint.setColor(Color.rgb(226, 232, 240));
            canvas.drawLine(MARGIN, y + 3, WIDTH - MARGIN, y + 3, paint);
            y += 11;
        }

        void write(Context context, Uri destination) throws Exception {
            finishPage();
            try (OutputStream output = context.getContentResolver().openOutputStream(destination)) {
                if (output == null) throw new IllegalStateException("No se pudo abrir el archivo seleccionado");
                document.writeTo(output);
            }
        }

        private void wrapped(String value, float size, int color, boolean bold, float lineHeight) {
            paint.setTextSize(size);
            paint.setColor(color);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL));
            for (String paragraph : value.split("\\n", -1)) {
                if (paragraph.isEmpty()) { y += lineHeight; continue; }
                StringBuilder line = new StringBuilder();
                for (String word : paragraph.split("\\s+")) {
                    String candidate = line.length() == 0 ? word : line + " " + word;
                    if (paint.measureText(candidate) > CONTENT_WIDTH && line.length() > 0) {
                        ensure(lineHeight + 3);
                        canvas.drawText(line.toString(), MARGIN, y, paint);
                        y += lineHeight;
                        line = new StringBuilder(word);
                    } else {
                        line = new StringBuilder(candidate);
                    }
                }
                if (line.length() > 0) {
                    ensure(lineHeight + 3);
                    canvas.drawText(line.toString(), MARGIN, y, paint);
                    y += lineHeight;
                }
            }
            y += 5;
        }

        private String trimToWidth(String value, float width) {
            if (paint.measureText(value) <= width) return value;
            String result = value;
            while (result.length() > 1 && paint.measureText(result + "…") > width) {
                result = result.substring(0, result.length() - 1);
            }
            return result + "…";
        }

        private void ensure(float height) {
            if (y + height <= HEIGHT - 42) return;
            finishPage();
            newPage();
        }

        private void newPage() {
            pageNumber++;
            page = document.startPage(new PdfDocument.PageInfo.Builder(WIDTH, HEIGHT, pageNumber).create());
            canvas = page.getCanvas();
            canvas.drawColor(Color.WHITE);
            y = MARGIN;
        }

        private void finishPage() {
            if (page == null) return;
            paint.setTextSize(9);
            paint.setTypeface(Typeface.DEFAULT);
            paint.setColor(Color.rgb(100, 116, 139));
            String footer = "App de mapas · Página " + pageNumber;
            canvas.drawText(footer, WIDTH - MARGIN - paint.measureText(footer), HEIGHT - 20, paint);
            document.finishPage(page);
            page = null;
            canvas = null;
        }

        @Override public void close() {
            if (page != null) finishPage();
            document.close();
        }
    }
}
