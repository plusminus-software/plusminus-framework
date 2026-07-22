package software.plusminus.framework;

import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static software.plusminus.check.Checks.check;

/* Covers plusminus-file: the /files controller works against the application's FileService
   while the request passes plusminus-security authentication. */
public class FileIntegrationTest extends FrameworkIntegrationTest {

    @Test
    public void uploadAndDownloadFile() {
        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource("file content".getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "test.txt";
            }
        });

        ResponseEntity<String> upload = restTemplate().exchange(web().url() + "/files/documents",
                HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

        check(upload.getStatusCodeValue()).is(200);
        assertThat(upload.getBody()).contains("documents/test.txt");

        ResponseEntity<String> download = httpGet("/files/documents/test.txt");

        check(download.getStatusCodeValue()).is(200);
        check(download.getBody()).is("file content");
    }
}
