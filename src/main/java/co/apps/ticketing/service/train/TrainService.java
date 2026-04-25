package co.apps.ticketing.service.train;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrainService {

    private final RestTemplate restTemplate;

    public Object getStationList() {
        try {
            // TAHAP 1: Mengetuk pintu (Hit halaman utama)
            // Ini bertujuan agar server mengirimkan 'Set-Cookie' awal
            HttpHeaders homeHeaders = new HttpHeaders();
            homeHeaders.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            HttpEntity<Void> homeEntity = new HttpEntity<>(homeHeaders);
            restTemplate.exchange("https://booking.kai.id/", HttpMethod.GET, homeEntity, String.class);

            // TAHAP 2: Hit API Station
            // RestTemplate akan otomatis membawa Cookie yang didapat dari Tahap 1
            HttpHeaders apiHeaders = new HttpHeaders();
            apiHeaders.setContentType(MediaType.APPLICATION_JSON);
            apiHeaders.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            apiHeaders.set("Referer", "https://booking.kai.id/");

            HttpEntity<Void> apiEntity = new HttpEntity<>(apiHeaders);
            String apiUrl = "https://booking.kai.id/api/stations2";

            ResponseEntity<Object> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    apiEntity,
                    Object.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("Berhasil mengambil data station!");
                return response.getBody();
            }

        } catch (Exception e) {
            System.err.println("Gagal bypass atau fetch data: " + e.getMessage());
        }

        return null;
    }
}
