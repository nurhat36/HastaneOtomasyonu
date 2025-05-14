package org.example.hastaneotomasyonu.Services;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import org.example.hastaneotomasyonu.models.Hasta;

public class VoiceAnnouncementService {
    private final Object voiceLock = new Object();
    private Hasta lastAnnouncedPatient;
    private String lastAnnouncedStatus;

    public void announcePatient(Hasta currentPatient) {
        if (shouldAnnouncePatient(currentPatient)) {
            String announcement = createAnnouncementText(currentPatient);
            speak(announcement);

            // Güncelleme yap
            lastAnnouncedPatient = currentPatient;
            lastAnnouncedStatus = getPatientStatus(currentPatient);
        }
    }

    private void speak(String text) {
        new Thread(() -> {
            synchronized (voiceLock) {
                speakWithFreeTTS(text);
            }
        }).start();
    }

    private void speakWithFreeTTS(String text) {
        try {
            System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
            VoiceManager voiceManager = VoiceManager.getInstance();
            Voice voice = voiceManager.getVoice("kevin16");

            if (voice != null) {
                voice.allocate();
                voice.setRate(150);
                voice.speak(text);
                voice.deallocate();
            }
        } catch (Exception e) {
            System.err.println("[FreeTTS HATASI] " + e.getMessage());
        }
    }

    private boolean shouldAnnouncePatient(Hasta currentPatient) {
        if (currentPatient == null) return false;

        if (lastAnnouncedPatient == null || !lastAnnouncedPatient.equals(currentPatient)) {
            return true;
        }

        String currentStatus = getPatientStatus(currentPatient);
        boolean statusChanged = !currentStatus.equals(lastAnnouncedStatus);

        boolean criticalChange = checkCriticalChange(currentPatient);

        return statusChanged || criticalChange;
    }

    private boolean checkCriticalChange(Hasta currentPatient) {
        if (lastAnnouncedPatient.kanamaliHastaDurumBilgisi == null &&
                currentPatient.kanamaliHastaDurumBilgisi != null) {
            return true;
        }
        return lastAnnouncedPatient.kanamaliHastaDurumBilgisi != null &&
                currentPatient.kanamaliHastaDurumBilgisi != null &&
                !lastAnnouncedPatient.kanamaliHastaDurumBilgisi.equals(
                        currentPatient.kanamaliHastaDurumBilgisi);
    }

    private String getPatientStatus(Hasta hasta) {
        return String.format("%s_%d_%s",
                hasta.hastaAdi,
                hasta.engellilikOrani,
                hasta.kanamaliHastaDurumBilgisi != null ? hasta.kanamaliHastaDurumBilgisi : "kanamaYok");
    }

    private String createAnnouncementText(Hasta hasta) {
        StringBuilder sb = new StringBuilder();
        sb.append("Patient ").append(hasta.hastaAdi).append(" is currently being examined.");

        if (hasta.engellilikOrani > 0) {
            sb.append(" Disability rate: ").append((int) hasta.engellilikOrani).append(" percent.");
        }

        if (hasta.kanamaliHastaDurumBilgisi != null) {
            if (hasta.kanamaliHastaDurumBilgisi.equalsIgnoreCase("agirKanama")) {
                sb.append(" Emergency! Severe bleeding detected!");
            } else if (!hasta.kanamaliHastaDurumBilgisi.equalsIgnoreCase("kanamaYok")) {
                sb.append(" Warning! Bleeding condition present.");
            }
        }

        return sb.toString();
    }
}