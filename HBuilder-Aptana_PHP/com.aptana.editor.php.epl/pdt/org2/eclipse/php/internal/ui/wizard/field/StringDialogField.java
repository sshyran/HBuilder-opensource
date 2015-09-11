/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org2.eclipse.php.internal.ui.wizard.field;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlCreator;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.aptana.core.util.StringUtil;

/**
 * Dialog field containing a label and a text control.
 */
public class StringDialogField extends DialogField
{

	protected String fText;
	protected Text fTextControl;
	protected ModifyListener fModifyListener;
	protected IContentProposalProvider fContentAssistProcessor;
	protected ContentProposalAdapter contentProposalAdapter;
	protected LabelProvider labelProvider;

	/**
	 * @param labelProvider
	 */
	public void setContentAssistLabelProvider(LabelProvider labelProvider)
	{
		this.labelProvider = labelProvider;
	}

	/**
	 * @return current content proposal provider
	 */
	public IContentProposalProvider getFContentAssistProcessor()
	{
		return fContentAssistProcessor;
	}

	/**
	 * @param contentAssistProcessor
	 */
	public void setFContentAssistProcessor(IContentProposalProvider contentAssistProcessor)
	{
		fContentAssistProcessor = contentAssistProcessor;
	}

	/**
	 * 
	 */
	public StringDialogField()
	{
		super();
		fText = StringUtil.EMPTY;
	}

	/**
	 * @see DialogField#doFillIntoGrid(org.eclipse.swt.widgets.Composite, int)
	 */
	// ------- layout helpers
	/*
	 * @see DialogField#doFillIntoGrid
	 */
	public Control[] doFillIntoGrid(Composite parent, int nColumns)
	{
		assertEnoughColumns(nColumns);

		Label label = getLabelControl(parent);
		label.setLayoutData(gridDataForLabel(1));
		Text text = getTextControl(parent);
		text.setLayoutData(gridDataForText(nColumns - 1));

		return new Control[] { label, text };
	}

	/*
	 * @see DialogField#getNumberOfControls
	 */
	/**
	 * @see DialogField#getNumberOfControls()
	 */
	public int getNumberOfControls()
	{
		return 2;
	}

	/**
	 * @param span
	 * @return GridData
	 */
	protected static GridData gridDataForText(int span)
	{
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = false;
		gd.horizontalSpan = span;
		return gd;
	}

	// ------- focus methods

	/*
	 * @see DialogField#setFocus
	 */
	/**
	 * @see DialogField#setFocus()
	 */
	public boolean setFocus()
	{
		if (isOkToUse(fTextControl))
		{
			fTextControl.setFocus();
			fTextControl.setSelection(0, fTextControl.getText().length());
		}
		return true;
	}

	// ------- ui creation

	/**
	 * Creates or returns the created text control.
	 * 
	 * @param parent
	 *            The parent composite or <code>null</code> when the widget has already been created.
	 * @return Text
	 */
	public Text getTextControl(Composite parent)
	{
		if (fContentAssistProcessor != null)
		{
			DecoratedField fld = new DecoratedField(parent, SWT.NONE, new IControlCreator()
			{

				public Control createControl(Composite parent, int style)
				{
					createText(parent);
					return fTextControl;
				}

			});
			FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
					FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
			fld.addFieldDecoration(fieldDecoration, SWT.TOP | SWT.LEFT, true);
			fld.showDecoration(fieldDecoration);
			final KeyStroke keyStroke = KeyStroke.getInstance(SWT.CONTROL, 32);

			contentProposalAdapter = new ContentProposalAdapter(fTextControl, new TextContentAdapter(),
					fContentAssistProcessor, keyStroke, null);
			contentProposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
			contentProposalAdapter.setLabelProvider(labelProvider);
			return fTextControl;
		}
		createText(parent);
		return fTextControl;
	}

	private void createText(Composite parent)
	{
		if (fTextControl == null)
		{
			assertCompositeNotNull(parent);
			fModifyListener = new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					doModifyText(e);
				}
			};

			fTextControl = new Text(parent, SWT.SINGLE | SWT.BORDER);
			// moved up due to 1GEUNW2
			fTextControl.setText(fText);
			fTextControl.setFont(parent.getFont());
			fTextControl.addModifyListener(fModifyListener);

			fTextControl.setEnabled(isEnabled());
			// if (fContentAssistProcessor != null)
			// {
			//
			// }
		}
	}

	protected void doModifyText(ModifyEvent e)
	{
		if (isOkToUse(fTextControl))
		{
			fText = fTextControl.getText();
		}
		dialogFieldChanged();
	}

	// ------ enable / disable management

	/*
	 * @see DialogField#updateEnableState
	 */
	/**
	 * @see DialogField#updateEnableState()
	 */
	protected void updateEnableState()
	{
		super.updateEnableState();
		if (isOkToUse(fTextControl))
		{
			fTextControl.setEnabled(isEnabled());
		}
	}

	// ------ text access

	/**
	 * Gets the text. Can not be <code>null</code>
	 * 
	 * @return String
	 */
	public String getText()
	{
		return fText;
	}

	/**
	 * Sets the text. Triggers a dialog-changed event.
	 * 
	 * @param text
	 */
	public void setText(String text)
	{
		fText = text;
		if (isOkToUse(fTextControl))
		{
			fTextControl.setText(text);
		}
		else
		{
			dialogFieldChanged();
		}
	}

	/**
	 * Sets the text without triggering a dialog-changed event.
	 * 
	 * @param text
	 */
	public void setTextWithoutUpdate(String text)
	{
		fText = text;
		if (isOkToUse(fTextControl))
		{
			fTextControl.removeModifyListener(fModifyListener);
			fTextControl.setText(text);
			fTextControl.addModifyListener(fModifyListener);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField#refresh()
	 */
	/**
	 * @see DialogField#refresh()
	 */
	public void refresh()
	{
		super.refresh();
		if (isOkToUse(fTextControl))
		{
			setTextWithoutUpdate(fText);
		}
	}

}
