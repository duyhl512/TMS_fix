package com.example.starter.handler;

import java.util.List;
import java.util.logging.Logger;

import com.example.starter.Util;
import com.example.starter.dao.AccountDAO;
import com.example.starter.dto.Profile;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;

public class AuthenticationHandler {
    private AuthenticationHandler() {}
    private static final String USERNAME = "username";
    private static final Logger _LOGGER = Logger.getLogger(AuthenticationHandler.class.getName());

    public static void login(RoutingContext rc, JWTAuth jwt) {
        rc.vertx().executeBlocking(blockingCodeHandler -> {
            try {
                JsonObject json = rc.body().asJsonObject();
                String username = json.getString(USERNAME);
                String password = json.getString("password");
                if (username == null || password == null) {
                    rc.response().setStatusCode(400).end();
                    return;
                }
                int accountId = AccountDAO.getAccountId(username, password);
                if (accountId == 0) {
                    rc.response().setStatusCode(401).end();
                    return;
                }
                rc.response().end(jwt.generateToken(
                        new JsonObject().put("sub", accountId).put(USERNAME, username),
                        new JWTOptions().setAlgorithm("RS256").setExpiresInMinutes(120)));
            } catch (Exception ex) {
                blockingCodeHandler.fail(ex);
            }
        }, false, null);
    }

    public static void profile(RoutingContext rc) {
        rc.vertx().executeBlocking(future -> {
            try {
                int accountId = Integer.parseInt(rc.user().principal().getString("sub"));
                String username = rc.user().principal().getString(USERNAME);
                List<String> functions = AccountDAO.getListFunction(accountId);
                Profile profile = new Profile();
                profile.setId(accountId);
                profile.setUsername(username);
                profile.setFunctions(functions);
                Util.sendResponse(rc, 200, profile);
            } catch (Exception e) {
                _LOGGER.severe(e.getMessage());
                Util.sendResponse(rc, 500, e.getMessage());
            }
        }, false, null);
    }
}
