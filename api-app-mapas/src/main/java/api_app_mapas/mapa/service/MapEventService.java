package api_app_mapas.mapa.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class MapEventService {
    private final CopyOnWriteArrayList<SseEmitter> clients = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(30L * 60L * 1000L);
        clients.add(emitter);
        emitter.onCompletion(() -> clients.remove(emitter));
        emitter.onTimeout(() -> clients.remove(emitter));
        emitter.onError(error -> clients.remove(emitter));
        try {
            emitter.send(SseEmitter.event().name("conectado").data("LISTO"));
        } catch (IOException error) {
            clients.remove(emitter);
            emitter.completeWithError(error);
        }
        return emitter;
    }

    public void publishAfterCommit(String contentType) {
        Runnable notification = () -> publish(contentType);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { notification.run(); }
            });
        } else {
            notification.run();
        }
    }

    private void publish(String contentType) {
        for (SseEmitter emitter : clients) {
            try {
                emitter.send(SseEmitter.event().name("mapa-actualizado")
                        .data(contentType + "|" + Instant.now()));
            } catch (Exception error) {
                clients.remove(emitter);
                emitter.complete();
            }
        }
    }
}
