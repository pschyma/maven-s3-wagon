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
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;

/**
 * This chain searches for AWS credentials in system properties -> environment variables -> ~/.m2/settings.xml -> Amazon's EC2 Instance Metadata Service
 */
public final class MavenAwsCredentialsProviderChain {

	public static AwsCredentialsProviderChain create() {
		return AwsCredentialsProviderChain.of(SystemPropertyCredentialsProvider.create(),
																					EnvironmentVariableCredentialsProvider.create(),
																					InstanceProfileCredentialsProvider.create());
	}

	public static AwsCredentialsProviderChain create(AuthenticationInfo auth) {
		return AwsCredentialsProviderChain.of(SystemPropertyCredentialsProvider.create(),
																					EnvironmentVariableCredentialsProvider.create(),
																					new AuthenticationInfoCredentialsProvider(auth),
																					InstanceProfileCredentialsProvider.create());
	}
}
