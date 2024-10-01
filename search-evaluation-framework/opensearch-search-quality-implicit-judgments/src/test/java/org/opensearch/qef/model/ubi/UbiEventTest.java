package org.opensearch.qef.model.ubi;

import com.google.gson.Gson;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opensearch.search.SearchHit;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

public class UbiEventTest {

    @Test
    @Disabled
    public void ubiEventFromJson() throws IOException, URISyntaxException {

        final InputStream inputStream = this.getClass().getResourceAsStream("/ubi_event.json");

        final Gson gson = new Gson();
        final SearchHit searchHit = gson.fromJson(new InputStreamReader(inputStream), SearchHit.class);

        final UbiEvent ubiEvent = new UbiEvent(searchHit);

    }

}
