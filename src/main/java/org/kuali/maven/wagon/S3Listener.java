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
package org.kuali.maven.wagon;

import org.apache.maven.wagon.events.SessionEvent;
import org.apache.maven.wagon.events.SessionListener;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Listen for events about the transfer and record timing and byte count information
 *
 * @author Jeff Caddel
 * @since May 27, 2010 5:08:12 PM
 */
public class S3Listener implements TransferListener, SessionListener {
	final Logger log = LoggerFactory.getLogger(S3Listener.class);
	SessionTracker sessionTracker = new SessionTracker();

	public void transferCompleted(final TransferEvent transferEvent) {
		TransferTracker tt = sessionTracker.getCurrentTransfer();
		tt.setCompleted(System.currentTimeMillis());
		// System.out.println();
		// log(tt.toString());
	}

	public void transferError(final TransferEvent transferEvent) {
		log.error("Transfer error: " + transferEvent.getException(), transferEvent.getException());
	}

	public void transferInitiated(final TransferEvent transferEvent) {
		sessionTracker.addTransfer(new TransferTracker());
		TransferTracker tt = sessionTracker.getCurrentTransfer();
		tt.setInitiated(System.currentTimeMillis());
	}

	public void transferProgress(final TransferEvent transferEvent, final byte[] buffer, final int length) {
		// No bytes were actually read
		if (length == -1) {
			return;
		}
		TransferTracker tt = sessionTracker.getCurrentTransfer();
		int byteCount = tt.getByteCount() + length;
		tt.setByteCount(byteCount);
	}

	public void transferStarted(final TransferEvent transferEvent) {
		TransferTracker tt = sessionTracker.getCurrentTransfer();
		tt.setStarted(System.currentTimeMillis());
		if (transferEvent.getRequestType() == TransferEvent.REQUEST_GET) {
			log.info("Downloading: " + getURI(transferEvent));
		} else {
			log.info("Uploading: " + getURI(transferEvent));
		}
		// System.out.print("[INFO] ");
	}

	protected String getURI(final TransferEvent event) {
		return getNormalizedURI(event.getWagon().getRepository().getUrl() + "/" + event.getResource().getName());
	}

	protected String getNormalizedURI(final String uri) {
		try {
			URI rawUri = new URI(uri);
			return rawUri.normalize().toString();
		} catch (URISyntaxException e) {
			return uri;
		}
	}

	/**
	 * @see SessionListener#sessionOpening(SessionEvent)
	 */
	public void sessionOpening(final SessionEvent sessionEvent) {
		sessionTracker.addSessionEvent(sessionEvent);
	}

	/**
	 * @see SessionListener#sessionOpened(SessionEvent)
	 */
	public void sessionOpened(final SessionEvent sessionEvent) {
		sessionTracker.addSessionEvent(sessionEvent);
		sessionTracker.setOpened(System.currentTimeMillis());
		// log(sessionEvent.getWagon().getRepository().getUrl() + " - Session: Opened  ");
	}

	/**
	 * @see SessionListener#sessionDisconnecting(SessionEvent)
	 */
	public void sessionDisconnecting(final SessionEvent sessionEvent) {
		sessionTracker.addSessionEvent(sessionEvent);
		sessionTracker.setDisconnecting(System.currentTimeMillis());
		// log(sessionEvent.getWagon().getRepository().getUrl() + " - Session: Disconnecting  ");
	}

	/**
	 * @see SessionListener#sessionDisconnected(SessionEvent)
	 */
	public void sessionDisconnected(final SessionEvent sessionEvent) {
		sessionTracker.addSessionEvent(sessionEvent);
		// log(sessionEvent.getWagon().getRepository().getUrl() + " - Disconnected");
		sessionTracker.setDisconnected(System.currentTimeMillis());

		int transferCount = sessionTracker.getTransfers().size();
		if (transferCount == 0) {
			return;
		}

		long byteCount = sessionTracker.getTransfers().stream().mapToLong(TransferTracker::getByteCount).sum();
		long elapsed = sessionTracker.getDisconnected() - sessionTracker.getOpened();
		Duration duration = Duration.of(elapsed, ChronoUnit.MILLIS);

		String sb =
			"Transfers: " + transferCount + " Time: " + duration + " Amount: " + bytesIntoHumanReadable(byteCount)
			// sb.append(" Rate: " + formatter.getRate(transferElapsed, byteCount));
			+ " Throughput: " + byteCount / duration.getSeconds() + " bytes/second";//.getRate// (elapsed, byteCount));
		log.info(sb);
	}

	/**
	 * @see SessionListener#sessionConnectionRefused(SessionEvent)
	 */
	public void sessionConnectionRefused(final SessionEvent sessionEvent) {
		sessionTracker.addSessionEvent(sessionEvent);
		log.warn(sessionEvent.getWagon().getRepository().getUrl() + " - Connection refused");
	}

	/**
	 * @see SessionListener#sessionLoggedIn(SessionEvent)
	 */
	public void sessionLoggedIn(final SessionEvent sessionEvent) {
		sessionTracker.addSessionEvent(sessionEvent);
		sessionTracker.setLoggedIn(System.currentTimeMillis());
		log.info("Logged in - " + sessionEvent.getWagon().getRepository().getHost());
	}

	/**
	 * @see SessionListener#sessionLoggedOff(SessionEvent)
	 */
	public void sessionLoggedOff(final SessionEvent sessionEvent) {
		sessionTracker.addSessionEvent(sessionEvent);
		sessionTracker.setLoggedOff(System.currentTimeMillis());
		log.info("Logged off - " + sessionEvent.getWagon().getRepository().getHost());
	}

	public void sessionError(final SessionEvent sessionEvent) {
		sessionTracker.addSessionEvent(sessionEvent);
		log.error("Session error: " + sessionEvent.getException(), sessionEvent.getException());
	}

	public void debug(final String message) {
		log.debug(message);
	}



	private String bytesIntoHumanReadable(long bytes) {
		long kilobyte = 1024;
		long megabyte = kilobyte * 1024;
		long gigabyte = megabyte * 1024;
		long terabyte = gigabyte * 1024;

		if ((bytes >= 0) && (bytes < kilobyte)) {
			return bytes + " B";

		} else if ((bytes >= kilobyte) && (bytes < megabyte)) {
			return (bytes / kilobyte) + " KB";

		} else if ((bytes >= megabyte) && (bytes < gigabyte)) {
			return (bytes / megabyte) + " MB";

		} else if ((bytes >= gigabyte) && (bytes < terabyte)) {
			return (bytes / gigabyte) + " GB";

		} else if (bytes >= terabyte) {
			return (bytes / terabyte) + " TB";

		} else {
			return bytes + " Bytes";
		}
	}


}
