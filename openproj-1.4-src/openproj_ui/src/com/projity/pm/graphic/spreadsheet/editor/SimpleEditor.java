/*
The contents of this file are subject to the Common Public Attribution License 
Version 1.0 (the "License"); you may not use this file except in compliance with 
the License. You may obtain a copy of the License at 
http://www.projity.com/license . The License is based on the Mozilla Public 
License Version 1.1 but Sections 14 and 15 have been added to cover use of 
software over a computer network and provide for limited attribution for the 
Original Developer. In addition, Exhibit A has been modified to be consistent 
with Exhibit B.

Software distributed under the License is distributed on an "AS IS" basis, 
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the 
specific language governing rights and limitations under the License. The 
Original Code is OpenProj. The Original Developer is the Initial Developer and 
is Projity, Inc. All portions of the code written by Projity are Copyright (c) 
2006, 2007. All Rights Reserved. Contributors Projity, Inc.

Alternatively, the contents of this file may be used under the terms of the 
Projity End-User License Agreeement (the Projity License), in which case the 
provisions of the Projity License are applicable instead of those above. If you 
wish to allow use of your version of this file only under the terms of the 
Projity License and not to allow others to use your version of this file under 
the CPAL, indicate your decision by deleting the provisions above and replace 
them with the notice and other provisions required by the Projity  License. If 
you do not delete the provisions above, a recipient may use your version of this 
file under either the CPAL or the Projity License.

[NOTE: The text of this license may differ slightly from the text of the notices 
in Exhibits A and B of the license at http://www.projity.com/license. You should 
use the latest text at http://www.projity.com/license for your modifications.
You may not remove this license text from the source files.]

Attribution Information: Attribution Copyright Notice: Copyright � 2006, 2007 
Projity, Inc. Attribution Phrase (not exceeding 10 words): Powered by OpenProj, 
an open source solution from Projity. Attribution URL: http://www.projity.com 
Graphic Image as provided in the Covered Code as file:  openproj_logo.png with 
alternatives listed on http://www.projity.com/logo

Display of Attribution Information is required in Larger Works which are defined 
in the CPAL as a work which combines Covered Code or portions thereof with code 
not governed by the terms of the CPAL. However, in addition to the other notice 
obligations, all copies of the Covered Code in Executable and Source Code form 
distributed must, as a form of attribution of the original author, include on 
each user interface screen the "OpenProj" logo visible to all users.  The 
OpenProj logo should be located horizontally aligned with the menu bar and left 
justified on the top left of the screen adjacent to the File menu.  The logo 
must be at least 100 x 25 pixels.  When users click on the "OpenProj" logo it 
must direct them back to http://www.projity.com.  
*/
package com.projity.pm.graphic.spreadsheet.editor;

import java.awt.Color;
import java.awt.Component;
import java.text.Format;
import java.text.ParseException;

import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.AbstractDocument;

import com.projity.datatype.CanSupplyRateUnit;
import com.projity.datatype.DurationFormat;
import com.projity.dialog.util.FixedSizeFilter;
import com.projity.field.Field;
import com.projity.field.FieldConverter;
import com.projity.field.FieldParseException;
import com.projity.pm.graphic.ChangeAwareTextField;
import com.projity.pm.graphic.spreadsheet.SpreadSheetModel;
import com.projity.pm.graphic.spreadsheet.common.CommonSpreadSheet;
/**
 *
 */
public class SimpleEditor extends DefaultCellEditor   {
	protected ChangeAwareTextField component;
	protected Class clazz;
	protected Format useFormat = null;
	JTable cachedTable = null;
	/**
	 * 
	 */
	public SimpleEditor() {
		super(new ChangeAwareTextField());
		component = (ChangeAwareTextField) getComponent();
		component.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT); // don't now if this is needed
		
		// this will make the enter key work properly. Otherwise, the enter does not go to the next line
		component.getInputMap(JComponent.WHEN_FOCUSED).getParent().getParent().getParent().remove(KeyStroke.getKeyStroke(10, 0));
		clazz = String.class;
		
	}
	public SimpleEditor(Class clazz) {
		this();
		this.clazz=clazz;
	}
	public SimpleEditor(Class arg0, Format arg1) {
		this(arg0);		
		useFormat = arg1;
	}
	
	
	
	
	/**
	 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
	 */
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean arg2, int row, int column) {
		cachedTable = table;
		String stringValue;
		if(value==null)
			stringValue=null;
		else 
			stringValue=FieldConverter.toString(value);
		
		component.setText(stringValue);
		//component.resetChange();
		component.setSelectedTextColor(Color.WHITE);
		component.setSelectionColor(Color.BLACK);
		
		if (table.getModel() instanceof SpreadSheetModel) {
			SpreadSheetModel model  = (SpreadSheetModel) table.getModel();
			Field field = model.getFieldInColumn(column+1);
			int width = field.getTextWidth(null,null);
			if (width != Integer.MAX_VALUE) {
				((AbstractDocument)component.getDocument()).setDocumentFilter(new FixedSizeFilter(width));
			}
			component.setHorizontalAlignment(field.getHorizontalAlignment());
			if (field.isWork()) {
				Object rowObject = model.getObjectInRow(row);
				if (rowObject instanceof CanSupplyRateUnit && ((CanSupplyRateUnit)rowObject).isMaterial())
					useFormat = DurationFormat.getNonTemporalWorkInstance();
			}
		} else {		
			if (value == null || value instanceof String ) 
				component.setHorizontalAlignment(JTextField.LEFT);
			else 
				component.setHorizontalAlignment(JTextField.RIGHT);
		}
		component.selectAll();
		
		return component;
	}
	

	public Object getCellEditorValue() {
		if (useFormat == null)
			try {
				return FieldConverter.convert(component.getText(),clazz,null);
			} catch (FieldParseException e1) {
				return null;
			}
		else
			try {
				return useFormat.parseObject(component.getText());
			} catch (ParseException e) {
				return null;
			}
	}
	
	
	
	
	public boolean stopCellEditing() {
		if (component.hasChanged()) {
			boolean result = super.stopCellEditing();
			if (handledPostErrorFocus())
				return false;
			return result;	
		}else{
			cancelCellEditing();
			return true;
		}
	}
	
	public void cancelCellEditing() {
		super.cancelCellEditing();
	}

	protected boolean handledPostErrorFocus() {
		if (cachedTable != null && cachedTable instanceof CommonSpreadSheet) {
			if (((CommonSpreadSheet)cachedTable).getLastException() != null) {
				cachedTable.requestFocus();
				return true;
			}
		}
		return false;

	}
	

	
}
