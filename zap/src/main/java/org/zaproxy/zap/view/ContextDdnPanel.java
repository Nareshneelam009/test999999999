/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.VerticalLayout;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.DataDrivenNode;

public class ContextDdnPanel extends AbstractContextPropertiesPanel {

	private static final long serialVersionUID = 1L;
	
    private static final String PANEL_NAME = Constant.messages.getString("context.ddn.panel.name");
    private static final String TITLE_LABEL = Constant.messages.getString("context.ddn.label.title");
    private static final String ADD_BUTTON_LABEL = Constant.messages.getString("context.ddn.button.add");
    private static final String MODIFY_BUTTON_LABEL = Constant.messages.getString("context.ddn.button.modify");
    private static final String REMOVE_BUTTON_LABEL = Constant.messages.getString("context.ddn.button.remove");
    private static final String REMOVE_CONFIRMATION_LABEL = Constant.messages.getString("context.ddn.checkbox.removeConfirmation");

    private JPanel mainPanel;
    private JTree ddnTree;
    private JButton addButton;
    private JButton modifyButton;
    private JButton removeButton;
    private JCheckBox removePromptCheckbox;
    
    private DefaultMutableTreeNode treeModel;

    public static String getPanelName(int contextId) {
        // Panel names have to be unique, so prefix with the context id
        return contextId + ": " + PANEL_NAME;
    }

    public ContextDdnPanel(Context context) {
        super(context.getId());
        
        this.treeModel = new DefaultMutableTreeNode(DataDrivenNode.ROOT_DDN);

        this.setLayout(new CardLayout());
        this.setName(getPanelName(this.getContextId()));
        this.add(getPanel(), mainPanel.getName());
    }

    private JPanel getPanel() {
        if (mainPanel == null) {
        	mainPanel = new JPanel();
        	mainPanel.setName("DataDrivenNodes");
        	mainPanel.setLayout(new VerticalLayout());
        	
        	mainPanel.add(new JLabel(TITLE_LABEL));
        	
        	JPanel treePanel = new JPanel();
        	treePanel.setLayout(new BorderLayout());
        	
        	ddnTree = new JTree();
        	ddnTree.setModel(new DefaultTreeModel(this.treeModel));
        	ddnTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        	ddnTree.addTreeSelectionListener(new TreeSelectionListener() {
				@Override
				public void valueChanged(TreeSelectionEvent e) {
					addButton.setEnabled(true);
				}
			});
        	
        	JScrollPane treeScrollPane = new JScrollPane();
        	treeScrollPane.setViewportView(ddnTree);
        	treeScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        	treePanel.add(treeScrollPane);
        	
        	JPanel buttonsPanel = new JPanel();
        	buttonsPanel.setLayout(new VerticalLayout());
        	addButton = new JButton(ADD_BUTTON_LABEL);
        	addButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					DataDrivenNodeDialog ddnDailog = 
							new DataDrivenNodeDialog(View.getSingleton().getSessionDialog(),
												     "context.ddn.dialog.add.title",
					                                 new Dimension(500, 200));
					
					DataDrivenNode parentDdn = DataDrivenNode.ROOT_DDN;
					TreePath parentNode = ddnTree.getSelectionPath();
					DefaultMutableTreeNode treeNode = treeModel;
					if (parentNode != null) {
						treeNode = (DefaultMutableTreeNode)parentNode.getLastPathComponent(); 
						parentDdn = (DataDrivenNode)treeNode.getUserObject(); 
					}
					
					DataDrivenNode newDdn = ddnDailog.showDialog(null);
					if (newDdn != null) {
						newDdn.setParentNode(parentDdn);
						parentDdn.addChildNode(newDdn);
						treeNode.add(new DefaultMutableTreeNode(newDdn));
					}
				}
			});
        	modifyButton = new JButton(MODIFY_BUTTON_LABEL);
        	removeButton = new JButton(REMOVE_BUTTON_LABEL);
        	addButton.setEnabled(false);
        	modifyButton.setEnabled(false);
        	removeButton.setEnabled(false);
        	buttonsPanel.add(addButton);
        	buttonsPanel.add(modifyButton);
        	buttonsPanel.add(removeButton);
        	treePanel.add(buttonsPanel, BorderLayout.EAST);
        	
        	removePromptCheckbox = new JCheckBox(REMOVE_CONFIRMATION_LABEL);
        	treePanel.add(removePromptCheckbox, BorderLayout.SOUTH);
        	
        	mainPanel.add(treePanel);
        }
        
        return mainPanel;
    }

    @Override
    public void initContextData(Session session, Context uiSharedContext) {
        // TODO Auto-generated method stub

    }

    @Override
    public void validateContextData(Session session) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveTemporaryContextData(Context uiSharedContext) {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveContextData(Session session) throws Exception {
        // TODO Auto-generated method stub

    }
    
    public static class DataDrivenNodeDialog extends StandardFieldsDialog {
    	
		private static final long serialVersionUID = 1L;
		
		private static final String FIELD_DDN_NAME = "context.ddn.dialog.ddnName";
		private static final String FIELD_PATTERN = "context.ddn.dialog.pattern";
		
		private DataDrivenNode data;

		public DataDrivenNodeDialog(JDialog owner, String titleLabel, Dimension dim) {
			super(owner, titleLabel, dim, true);
		}
		
		public DataDrivenNode showDialog(DataDrivenNode data) {
			this.data = data;
			
			String ddnName = "";
			String pattern = "";
			
			if (this.data != null) {
				ddnName = this.data.getName();
				Pattern regexPattern = this.data.getPattern();
				
				if (regexPattern != null) {
					pattern = regexPattern.pattern();
				}
			}
			
			this.addTextField(FIELD_DDN_NAME, ddnName);
			this.addTextField(FIELD_PATTERN, pattern);
			
			this.setVisible(true);
			
			return this.data;
		}

		@Override
		public void save() {
			DataDrivenNode parent = null;
			if (this.data != null) {
				parent = this.data.getParentNode();
			}
			
			this.data = new DataDrivenNode(this.getStringValue(FIELD_DDN_NAME), this.getStringValue(FIELD_PATTERN), parent);
		}

		@Override
		public String validateFields() {
			if (!this.getStringValue(FIELD_DDN_NAME).matches("[A-Za-z0-9_]+")) {
                return Constant.messages.getString("context.ddn.dialog.error.ddnName");
            }
			
			String pattern = this.getStringValue(FIELD_PATTERN);
			if (this.isEmptyField(FIELD_PATTERN)) {
				// TODO (JMG) : Add Check to ensure the appropriate Pattern Format (ie. 2 matching groups, one nested inside the other?)
				return Constant.messages.getString("context.ddn.dialog.error.pattern");
			}
			
			try {
				Pattern.compile(pattern);
			}
			catch (Exception exception) {
				return Constant.messages.getString("context.ddn.dialog.error.pattern");
			}
			
			return null;
		}
    }
}
