package software.plusminus.framework.fixtures;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import software.plusminus.file.exception.FileNotFoundException;
import software.plusminus.file.service.FileService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Profile("test")
public class InMemoryFileService implements FileService {

    private final Map<String, byte[]> files = new ConcurrentHashMap<>();

    @Override
    public InputStream download(String filepath) {
        byte[] content = files.get(filepath);
        if (content == null) {
            throw new FileNotFoundException("File " + filepath + " is not found");
        }
        return new ByteArrayInputStream(content);
    }

    @Override
    public URL upload(String filepath, InputStream data, long contentLength) {
        try {
            files.put(filepath, StreamUtils.copyToByteArray(data));
            return new URL("http://localhost/files/" + filepath);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void remove(String filepath) {
        files.remove(filepath);
    }

    @Override
    public List<String> getAllFilePaths(String folderPath) {
        return files.keySet().stream()
                .filter(path -> path.startsWith(folderPath))
                .collect(Collectors.toList());
    }
}
