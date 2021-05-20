/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;

@JsonInclude(Include.NON_NULL)
/**
 * Describes JWT verifier configuration
 *
 * @author Mykhailo Kuznietsov
 */
public class VerifierConfig {
  private String upstream;

  private String audience;

  @JsonProperty("max_skew")
  private String maxSkew;

  @JsonProperty("max_ttl")
  private String maxTtl;

  @JsonProperty("nonce_storage")
  private RegistrableComponentConfig nonceStorage;

  @JsonProperty("key_server")
  private RegistrableComponentConfig keyServer;

  @JsonProperty("claims_verifiers")
  private Set<RegistrableComponentConfig> claimsVerifiers;

  @JsonProperty("auth_redirect_url")
  private String authUrl;

  @JsonProperty("auth_cookies_enabled")
  private boolean cookiesEnabled;

  private Set<String> excludes;

  @JsonProperty("public_base_path")
  private String publicBasePath;

  @JsonProperty("cookie_path")
  private String cookiePath;

  public String getAudience() {
    return audience;
  }

  public void setAudience(String audience) {
    this.audience = audience;
  }

  public VerifierConfig withAudience(String audience) {
    this.audience = audience;
    return this;
  }

  public String getMaxSkew() {
    return maxSkew;
  }

  public void setMaxSkew(String maxSkew) {
    this.maxSkew = maxSkew;
  }

  public VerifierConfig withMaxSkew(String maxSkew) {
    this.maxSkew = maxSkew;
    return this;
  }

  public String getMaxTtl() {
    return maxTtl;
  }

  public void setMaxTtl(String maxTtl) {
    this.maxTtl = maxTtl;
  }

  public VerifierConfig withMaxTtl(String maxTtl) {
    this.maxTtl = maxTtl;
    return this;
  }

  public String getUpstream() {
    return upstream;
  }

  public void setUpstream(String upstream) {
    this.upstream = upstream;
  }

  public VerifierConfig withUpstream(String upstream) {
    this.upstream = upstream;
    return this;
  }

  public RegistrableComponentConfig getNonceStorage() {
    return nonceStorage;
  }

  public void setNonceStorage(RegistrableComponentConfig nonceStorage) {
    this.nonceStorage = nonceStorage;
  }

  public VerifierConfig withNonceStorage(RegistrableComponentConfig nonceStorage) {
    this.nonceStorage = nonceStorage;
    return this;
  }

  public RegistrableComponentConfig getKeyServer() {
    return keyServer;
  }

  public void setKeyServer(RegistrableComponentConfig keyServer) {
    this.keyServer = keyServer;
  }

  public VerifierConfig withKeyServer(RegistrableComponentConfig keyServer) {
    this.keyServer = keyServer;
    return this;
  }

  public Set<RegistrableComponentConfig> getClaimsVerifiers() {
    return claimsVerifiers;
  }

  public void setClaimsVerifiers(Set<RegistrableComponentConfig> claimsVerifiers) {
    this.claimsVerifiers = claimsVerifiers;
  }

  public VerifierConfig withClaimsVerifier(Set<RegistrableComponentConfig> claimsVerifiers) {
    this.claimsVerifiers = claimsVerifiers;
    return this;
  }

  public Set<String> getExcludes() {
    return excludes;
  }

  public void setExcludes(Set<String> excludes) {
    this.excludes = excludes;
  }

  public VerifierConfig withExcludes(Set<String> excludes) {
    this.excludes = excludes;
    return this;
  }

  public String getAuthUrl() {
    return authUrl;
  }

  public void setAuthUrl(String authUrl) {
    this.authUrl = authUrl;
  }

  public VerifierConfig withAuthUrl(String authUrl) {
    this.authUrl = authUrl;
    return this;
  }

  public boolean getCookiesEnabled() {
    return cookiesEnabled;
  }

  public VerifierConfig setCookiesEnabled(boolean cookiesEnabled) {
    this.cookiesEnabled = cookiesEnabled;
    return this;
  }

  public VerifierConfig withCookiesEnabled(boolean cookiesEnabled) {
    this.cookiesEnabled = cookiesEnabled;
    return this;
  }

  public String getPublicBasePath() {
    return publicBasePath;
  }

  public void setPublicBasePath(String publicBasePath) {
    this.publicBasePath = publicBasePath;
  }

  public VerifierConfig withPublicBasePath(String publicBasePath) {
    this.publicBasePath = publicBasePath;
    return this;
  }

  public String getCookiePath() {
    return cookiePath;
  }

  public void setCookiePath(String cookiePath) {
    this.cookiePath = cookiePath;
  }

  public VerifierConfig withCookiePath(String cookiePath) {
    this.cookiePath = cookiePath;
    return this;
  }
}
