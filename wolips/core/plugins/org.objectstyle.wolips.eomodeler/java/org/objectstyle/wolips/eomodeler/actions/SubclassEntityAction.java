/*
 * ====================================================================
 * 
 * The ObjectStyle Group Software License, Version 1.0
 * 
 * Copyright (c) 2006 The ObjectStyle Group and individual authors of the
 * software. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 * include the following acknowlegement: "This product includes software
 * developed by the ObjectStyle Group (http://objectstyle.org/)." Alternately,
 * this acknowlegement may appear in the software itself, if and wherever such
 * third-party acknowlegements normally appear.
 * 
 * 4. The names "ObjectStyle Group" and "Cayenne" must not be used to endorse or
 * promote products derived from this software without prior written permission.
 * For written permission, please contact andrus@objectstyle.org.
 * 
 * 5. Products derived from this software may not be called "ObjectStyle" nor
 * may "ObjectStyle" appear in their names without prior written permission of
 * the ObjectStyle Group.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * OBJECTSTYLE GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many individuals on
 * behalf of the ObjectStyle Group. For more information on the ObjectStyle
 * Group, please see <http://objectstyle.org/>.
 *  
 */
package org.objectstyle.wolips.eomodeler.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.objectstyle.wolips.eomodeler.Messages;
import org.objectstyle.wolips.eomodeler.editors.entity.SubclassEntityDialog;
import org.objectstyle.wolips.eomodeler.model.DuplicateNameException;
import org.objectstyle.wolips.eomodeler.model.EOEntity;
import org.objectstyle.wolips.eomodeler.model.EOModel;
import org.objectstyle.wolips.eomodeler.model.IEOEntityRelative;
import org.objectstyle.wolips.eomodeler.model.InheritanceType;
import org.objectstyle.wolips.eomodeler.utils.ErrorUtils;

public class SubclassEntityAction implements IWorkbenchWindowActionDelegate {
	private EOEntity myEntity;

	private EOModel myModel;

	private IWorkbenchWindow myWindow;

	public void init(IWorkbenchWindow _window) {
		myWindow = _window;
	}

	public void dispose() {
		// DO NOTHING
	}

	public void selectionChanged(IAction _action, ISelection _selection) {
		myModel = null;
		myEntity = null;
		if (_selection instanceof IStructuredSelection) {
			Object selectedObject = ((IStructuredSelection) _selection).getFirstElement();
			if (selectedObject instanceof EOModel) {
				myModel = (EOModel) selectedObject;
			} else if (selectedObject instanceof IEOEntityRelative) {
				myEntity = ((IEOEntityRelative) selectedObject).getEntity();
				myModel = myEntity.getModel();
			}
		}
	}

	public void run(IAction _action) {
		if (myModel != null) {
			SubclassEntityDialog dialog = new SubclassEntityDialog(myWindow.getShell(), myModel, myEntity);
			dialog.setBlockOnOpen(true);
			int results = dialog.open();
			if (results == Window.OK) {
				String entityName = dialog.getEntityName();
				if (entityName != null && entityName.trim().length() > 0) {
					EOEntity parentEntity = dialog.getParentEntity();
					InheritanceType inheritanceType = dialog.getInheritanceType();
					try {
						EOEntity newEntity = parentEntity.subclass(entityName, inheritanceType);
						newEntity.setRestrictingQualifier(dialog.getRestrictingQualifier());
						parentEntity.getModel().addEntity(newEntity);
					} catch (DuplicateNameException e) {
						ErrorUtils.openErrorDialog(Display.getDefault().getActiveShell(), e);
					}
				} else {
					MessageDialog.openError(myWindow.getShell(), Messages.getString("Subclass.noEntityNameTitle"), Messages.getString("Subclass.noEntityNameMessage"));//$NON-NLS-1$
				}
			}
		} else {
			MessageDialog.openError(myWindow.getShell(), Messages.getString("Subclass.noModelSelectedTitle"), Messages.getString("Subclass.noModelSelectedMessage"));//$NON-NLS-1$
		}
	}
}
