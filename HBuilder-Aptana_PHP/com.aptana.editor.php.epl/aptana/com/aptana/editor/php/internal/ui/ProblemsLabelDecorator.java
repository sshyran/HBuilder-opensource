package com.aptana.editor.php.internal.ui;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.editor.php.internal.ui.viewsupport.IProblemChangedListener;

/**
 * A lightweight label decorator that looks into IResource markers and decorate with errors or warnings.
 */
public class ProblemsLabelDecorator implements ILightweightLabelDecorator
{

	private static final int ERROR = 0;
	private static final int WARNING = 1;
	private ListenerList fListeners;
	private IProblemChangedListener fProblemChangedListener;

	/**
	 * This is a special <code>LabelProviderChangedEvent</code> carrying additional information whether the event
	 * origins from a maker change.
	 * <p>
	 * <code>ProblemsLabelChangedEvent</code>s are only generated by <code>
	 * ProblemsLabelDecorator</code>s.
	 * </p>
	 */
	public static class ProblemsLabelChangedEvent extends LabelProviderChangedEvent
	{

		private static final long serialVersionUID = 1L;

		private boolean fMarkerChange;

		/**
		 * @param eventSource
		 *            the base label provider
		 * @param changedResource
		 *            the changed resources
		 * @param isMarkerChange
		 *            <code>true</code> if the change is a marker change; otherwise <code>false</code>
		 */
		public ProblemsLabelChangedEvent(IBaseLabelProvider eventSource, IResource[] changedResource,
				boolean isMarkerChange)
		{
			super(eventSource, changedResource);
			fMarkerChange = isMarkerChange;
		}

		/**
		 * Returns whether this event origins from marker changes. If <code>false</code> an annotation model change is
		 * the origin. In this case viewers not displaying working copies can ignore these events.
		 * 
		 * @return if this event origins from a marker change.
		 */
		public boolean isMarkerChange()
		{
			return fMarkerChange;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose()
	{
		if (fProblemChangedListener != null)
		{
			PHPEplPlugin.getDefault().getProblemMarkerManager().removeListener(fProblemChangedListener);
			fProblemChangedListener = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property)
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see IBaseLabelProvider#addListener(ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener)
	{
		if (fListeners == null)
		{
			fListeners = new ListenerList();
		}
		fListeners.add(listener);
		if (fProblemChangedListener == null)
		{
			fProblemChangedListener = new IProblemChangedListener()
			{
				public void problemsChanged(IResource[] changedResources, boolean isMarkerChange)
				{
					fireProblemsChanged(changedResources, isMarkerChange);
				}
			};
			PHPEplPlugin.getDefault().getProblemMarkerManager().addListener(fProblemChangedListener);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see IBaseLabelProvider#removeListener(ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener)
	{
		if (fListeners != null)
		{
			fListeners.remove(listener);
			if (fListeners.isEmpty() && fProblemChangedListener != null)
			{
				PHPEplPlugin.getDefault().getProblemMarkerManager().removeListener(fProblemChangedListener);
				fProblemChangedListener = null;
			}
		}
	}

	private void fireProblemsChanged(IResource[] changedResources, boolean isMarkerChange)
	{
		if (fListeners != null && !fListeners.isEmpty())
		{
			LabelProviderChangedEvent event = new ProblemsLabelChangedEvent(this, changedResources, isMarkerChange);
			Object[] listeners = fListeners.getListeners();
			for (int i = 0; i < listeners.length; i++)
			{
				((ILabelProviderListener) listeners[i]).labelProviderChanged(event);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object,
	 * org.eclipse.jface.viewers.IDecoration)
	 */
	public void decorate(Object element, IDecoration decoration)
	{
		int adornmentFlags = computeAdornmentFlags(element);
		switch (adornmentFlags)
		{
			case ERROR:
				decoration.addOverlay(PHPPluginImages.DESC_OVR_ERROR);
				break;
			case WARNING:
				decoration.addOverlay(PHPPluginImages.DESC_OVR_WARNING);
				break;
		}
	}

	/**
	 * Compute the flags that were set on the give object. We expect an IResource for this computation, and we return
	 * the flags according to the warnings or errors set on the resource.
	 * 
	 * @param obj
	 *            A IResource (expected)
	 * @return An integer representing an ERROR or a WARNING; -1 if the give object was not an IResource or when we got
	 *         an exception retrieving the {@link IMarker}s from the resource.
	 */
	protected int computeAdornmentFlags(Object obj)
	{
		try
		{
			if (obj instanceof IResource)
			{
				return getErrorTicksFromMarkers((IResource) obj, IResource.DEPTH_INFINITE);
			}
		}
		catch (CoreException e)
		{
			if (e.getStatus().getCode() == IResourceStatus.MARKER_NOT_FOUND)
			{
				return -1;
			}
			IdeLog.logWarning(PHPEplPlugin.getDefault(),
					"Error computing PHP label-decoration adornment flags", e, PHPEplPlugin.DEBUG_SCOPE); //$NON-NLS-1$
		}
		return -1;
	}

	private int getErrorTicksFromMarkers(IResource res, int depth) throws CoreException
	{
		if (res == null || !res.isAccessible())
		{
			return -1;
		}
		int info = -1;

		IMarker[] markers = res.findMarkers(IMarker.PROBLEM, true, depth);
		if (markers != null)
		{
			for (int i = 0; i < markers.length && info != ERROR; i++)
			{
				IMarker curr = markers[i];
				int priority = curr.getAttribute(IMarker.SEVERITY, -1);
				if (priority == IMarker.SEVERITY_WARNING)
				{
					info = WARNING;
				}
				else if (priority == IMarker.SEVERITY_ERROR)
				{
					info = ERROR;
				}
			}
		}
		return info;
	}
}
