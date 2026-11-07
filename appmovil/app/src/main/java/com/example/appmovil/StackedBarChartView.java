package com.example.appmovil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** Barras verticales apiladas: una columna por distrito y un color por incidente. */
public class StackedBarChartView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<String> districts=new ArrayList<>(),types=new ArrayList<>();
    private long[][] values=new long[0][0];
    private final int[] colors={0xFF0284C7,0xFF0D9488,0xFFF59E0B,0xFFEF4444,0xFF8B5CF6,0xFFEC4899,0xFF14B8A6};
    private final float density,scaledDensity;

    public StackedBarChartView(Context context){this(context,null);}
    public StackedBarChartView(Context context,@Nullable AttributeSet attrs){super(context,attrs);density=getResources().getDisplayMetrics().density;scaledDensity=getResources().getDisplayMetrics().scaledDensity;paint.setTypeface(android.graphics.Typeface.create("sans",android.graphics.Typeface.NORMAL));}

    public void setData(List<String> districtLabels,List<String> typeLabels,long[][] newValues){
        districts.clear();types.clear();if(districtLabels!=null)districts.addAll(districtLabels);if(typeLabels!=null)types.addAll(typeLabels);
        values=newValues==null?new long[0][0]:newValues;setContentDescription(description());requestLayout();invalidate();
    }

    @Override protected void onMeasure(int widthSpec,int heightSpec){int legendRows=(int)Math.ceil(types.size()/2d);setMeasuredDimension(resolveSize(dp(300),widthSpec),resolveSize(dp(250+legendRows*24),heightSpec));}

    @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);if(districts.isEmpty()||types.isEmpty())return;
        float left=dp(34),right=getWidth()-dp(10),top=dp(28);int legendRows=(int)Math.ceil(types.size()/2d);float bottom=getHeight()-dp(55+legendRows*24);
        long max=1;long[] totals=new long[districts.size()];for(int d=0;d<districts.size();d++){for(int t=0;t<types.size();t++)totals[d]+=value(d,t);max=Math.max(max,totals[d]);}
        paint.setStrokeWidth(dp(1));paint.setTextSize(sp(9));for(int i=0;i<=4;i++){float y=bottom-(bottom-top)*i/4f;paint.setColor(0xFFE2E8F0);canvas.drawLine(left,y,right,y,paint);paint.setColor(0xFF64748B);canvas.drawText(String.valueOf(Math.round(max*i/4f)),dp(2),y+sp(3),paint);}
        float slot=(right-left)/districts.size(),bar=Math.min(dp(48),slot*.62f);
        for(int d=0;d<districts.size();d++){float center=left+slot*(d+.5f),cursor=bottom;for(int t=0;t<types.size();t++){long value=value(d,t);if(value<=0)continue;float height=(bottom-top)*value/(float)max;paint.setColor(colors[t%colors.length]);canvas.drawRect(center-bar/2,cursor-height,center+bar/2,cursor,paint);cursor-=height;}centerText(canvas,String.valueOf(totals[d]),center,Math.max(top+sp(9),cursor-dp(6)),10,0xFF0F172A);drawDistrict(canvas,districts.get(d),center,bottom+dp(17));}
        float legendTop=bottom+dp(48),columnWidth=(right-left)/2f;for(int i=0;i<types.size();i++){int column=i%2,row=i/2;float x=left+column*columnWidth,y=legendTop+row*dp(24);paint.setColor(colors[i%colors.length]);canvas.drawCircle(x+dp(5),y,dp(5),paint);paint.setColor(0xFF475569);paint.setTextSize(sp(10));canvas.drawText(shortLabel(types.get(i),20),x+dp(15),y+sp(3),paint);}
    }

    private void drawDistrict(Canvas canvas,String value,float center,float y){String first=value,second="";if(value.length()>12){int split=value.lastIndexOf(' ',Math.min(12,value.length()-1));if(split>3){first=value.substring(0,split);second=value.substring(split+1);}}centerText(canvas,first,center,y,9,0xFF475569);if(!second.isEmpty())centerText(canvas,second,center,y+dp(12),9,0xFF475569);}
    private long value(int district,int type){return district<values.length&&type<values[district].length?Math.max(0,values[district][type]):0;}
    private void centerText(Canvas canvas,String text,float x,float y,int size,int color){paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(sp(size));paint.setColor(color);canvas.drawText(text,x,y,paint);paint.setTextAlign(Paint.Align.LEFT);}
    private String shortLabel(String value,int max){return value.length()<=max?value:value.substring(0,max-1)+"…";}
    private String description(){StringBuilder out=new StringBuilder("Incidentes por distrito. ");for(int d=0;d<districts.size();d++){if(d>0)out.append("; ");out.append(districts.get(d)).append(": ");for(int t=0;t<types.size();t++){if(t>0)out.append(", ");out.append(types.get(t)).append(' ').append(value(d,t));}}return out.toString();}
    private int dp(int value){return Math.round(value*density);}private float sp(int value){return value*scaledDensity;}
}
