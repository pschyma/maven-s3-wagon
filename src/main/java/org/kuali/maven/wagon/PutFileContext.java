/**
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
package org.kuali.maven.wagon;

import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.resource.Resource;
import software.amazon.awssdk.services.s3.S3AsyncClient;

import java.io.File;

/**
 * This is the context needed by the Wagon for uploading a file and tracking its progress as it goes
 */
public class PutFileContext {
	private File source;
	private String destination;
	private Resource resource;
	private TransferProgress progress;
	private TransferListenerSupport listeners;
	private RequestFactory factory;
	private S3AsyncClient client;

	public void fireStart() {
		listeners.fireTransferInitiated(getResource(), TransferEvent.REQUEST_PUT);
		listeners.fireTransferStarted(getResource(), TransferEvent.REQUEST_PUT);
	}

	public void fireComplete() {
		listeners.fireTransferCompleted(getResource(), TransferEvent.REQUEST_PUT);
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public TransferProgress getProgress() {
		return progress;
	}

	public void setProgress(TransferProgress progress) {
		this.progress = progress;
	}

	public TransferListenerSupport getListeners() {
		return listeners;
	}

	public void setListeners(TransferListenerSupport listeners) {
		this.listeners = listeners;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public File getSource() {
		return source;
	}

	public void setSource(File source) {
		this.source = source;
	}

	public RequestFactory getFactory() {
		return factory;
	}

	public void setFactory(RequestFactory factory) {
		this.factory = factory;
	}

	public S3AsyncClient getClient() {
		return client;
	}

	public void setClient(S3AsyncClient client) {
		this.client = client;
	}

}
