/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.handler;

import org.eclipse.osgi.util.NLS;

/**
 * @author Shalom Gibly <sgibly@appcelerator.com>
 */
public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.aptana.editor.php.internal.ui.handler.messages"; //$NON-NLS-1$
	public static String OpenTypeHandler_openTypeMessage;
	public static String OpenTypeHandler_openTypeTitle;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}