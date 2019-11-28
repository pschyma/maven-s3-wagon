/*
 * Copyright 2010-2015 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.maven.wagon.auth;

import org.apache.maven.wagon.authentication.AuthenticationInfo;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import javax.annotation.Nonnull;
import java.util.Optional;

public final class AuthenticationInfoCredentialsProvider implements AwsCredentialsProvider {

	private final AuthenticationInfo auth;

	public AuthenticationInfoCredentialsProvider(@Nonnull AuthenticationInfo auth) {
		this.auth = auth;
	}

	@Override
	public AwsCredentials resolveCredentials() {
		String accessKey = Optional.ofNullable(auth.getUserName()).orElse("").trim();
		String secretKey = Optional.ofNullable(auth.getPassword()).orElse("").trim();

		if (accessKey.isEmpty() || secretKey.isEmpty()) {
			throw new IllegalArgumentException(getAuthenticationErrorMessage());
		}

		return AwsBasicCredentials.create(accessKey, secretKey);
	}

	public void refresh() {
		// no-op
	}

	protected String getAuthenticationErrorMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("The S3 wagon needs AWS Access Key set as the username and AWS Secret Key set as the password. eg:\n");
		sb.append("<server>\n");
		sb.append("  <id>my.server</id>\n");
		sb.append("  <username>[AWS Access Key ID]</username>\n");
		sb.append("  <password>[AWS Secret Access Key]</password>\n");
		sb.append("</server>\n");
		return sb.toString();
	}

}
