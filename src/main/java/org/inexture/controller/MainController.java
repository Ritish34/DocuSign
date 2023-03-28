package org.inexture.controller;

import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.ApiException;
import com.docusign.esign.model.EnvelopeDefinition;
import com.docusign.esign.model.RecipientViewRequest;
import com.docusign.esign.model.ViewUrl;
import org.inexture.DSConfiguration;
import org.inexture.core.Session;
import org.inexture.core.User;
import org.inexture.model.FormData;
import org.inexture.services.EmbeddedSigningService;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;

@Controller
public class MainController {
    private static final String DOCUMENT_FILE_NAME = "World_Wide_Corp_lorem.pdf";
    private static final String DOCUMENT_NAME = "Hello World";
    private static final int ANCHOR_OFFSET_Y = 20;
    private static final int ANCHOR_OFFSET_X = 10;
    private static final String SIGNER_CLIENT_ID = "1000";

    private static final String RETURN_URL = "http://localhost:8080/ds-return";
    private final Session session;
    private final User user;

    protected final DSConfiguration config;

    protected static final String BEARER_AUTHENTICATION = "Bearer ";

    public MainController(Session session, User user, DSConfiguration config) {
        this.session = session;
        this.user = user;
        this.config = config;
    }

    @RequestMapping(path = "/home")
    public String hello(Model model){
        return "index";
    }

    @RequestMapping(path = "/end")
    public String sendDoc(){
        return "End";
    }

        @RequestMapping("/send")
        public Object signDoc(@ModelAttribute("index") FormData data) throws IOException, ApiException {
            String signerName = data.getRname();
            String signerEmail = data.getRemail();
            String accountId = session.getAccountId();

            // Step 1. Create the envelope definition
            EnvelopeDefinition envelope = EmbeddedSigningService.makeEnvelope(
                    signerEmail,
                    signerName,
                    SIGNER_CLIENT_ID,
                    ANCHOR_OFFSET_Y,
                    ANCHOR_OFFSET_X,
                    DOCUMENT_FILE_NAME,
                    DOCUMENT_NAME);

            // Step 2. Call DocuSign to create the envelope
            ApiClient apiClient = createApiClient(session.getBasePath(), user.getAccessToken());

            String envelopeId = EmbeddedSigningService.createEnvelope(apiClient, session.getAccountId(), envelope);
            session.setEnvelopeId(envelopeId);

            // Step 3. create the recipient view, the embedded signing
            RecipientViewRequest viewRequest = EmbeddedSigningService.makeRecipientViewRequest(
                    signerEmail,
                    signerName,
                    SIGNER_CLIENT_ID,
                    RETURN_URL,
                    config.getDsPingUrl());

            ViewUrl viewUrl = EmbeddedSigningService.embeddedSigning(
                    apiClient,
                    accountId,
                    envelopeId,
                    viewRequest
            );

            // Step 4. Redirect the user to the embedded signing
            // Don't use an iFrame!
            // State can be stored/recovered using the framework's session or a
            // query parameter on the returnUrl (see the makeRecipientViewRequest method)
            return new RedirectView(viewUrl.getUrl());
        }

    protected static ApiClient createApiClient(String basePath, String userAccessToken) {
        ApiClient apiClient = new ApiClient(basePath);
        apiClient.addDefaultHeader(HttpHeaders.AUTHORIZATION, BEARER_AUTHENTICATION + userAccessToken);
        return apiClient;
    }
}
