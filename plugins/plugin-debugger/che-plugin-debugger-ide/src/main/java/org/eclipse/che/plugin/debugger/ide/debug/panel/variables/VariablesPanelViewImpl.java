/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.debugger.ide.debug.panel.variables;

import static org.eclipse.che.ide.ui.smartTree.SelectionModel.Mode.SINGLE;
import static org.eclipse.che.ide.ui.smartTree.SortDir.ASC;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.WatchExpression;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.status.StatusText;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.DebuggerNodeFactory;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.VariableNode;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.WatchExpressionNode;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.comparator.DebugNodeTypeComparator;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.comparator.VariableNodeComparator;
import org.eclipse.che.plugin.debugger.ide.debug.tree.node.key.DebugNodeUniqueKeyProvider;

public class VariablesPanelViewImpl extends BaseView<VariablesPanelView.ActionDelegate>
    implements VariablesPanelView {
  interface VariablesPanelViewImplUiBinder extends UiBinder<Widget, VariablesPanelViewImpl> {}

  @UiField(provided = true)
  DebuggerLocalizationConstant locale;

  @UiField(provided = true)
  Resources coreRes;

  @UiField(provided = true)
  Tree tree;

  @UiField SimplePanel watchExpressionPanel;

  private final DebuggerNodeFactory nodeFactory;
  private final DebugNodeUniqueKeyProvider nodeKeyProvider;

  @Inject
  public VariablesPanelViewImpl(
      DebuggerResources resources,
      DebuggerLocalizationConstant locale,
      VariablesPanelViewImplUiBinder uiBinder,
      Resources coreRes,
      DebuggerNodeFactory nodeFactory,
      DebugNodeUniqueKeyProvider nodeKeyProvider) {
    this.locale = locale;
    this.coreRes = coreRes;
    this.nodeFactory = nodeFactory;
    this.nodeKeyProvider = nodeKeyProvider;

    StatusText<Tree> emptyTreeStatus = new StatusText<>();
    emptyTreeStatus.setText("");
    tree = new Tree(new NodeStorage(nodeKeyProvider), new NodeLoader(), emptyTreeStatus);

    setContentWidget(uiBinder.createAndBindUi(this));

    tree.ensureDebugId("debugger-tree");

    tree.getSelectionModel().setSelectionMode(SINGLE);

    tree.addExpandHandler(
        event -> {
          Node expandedNode = event.getNode();
          if (expandedNode instanceof VariableNode) {
            delegate.onExpandVariable(((VariableNode) expandedNode).getData());
          }
        });

    tree.getNodeStorage()
        .addSortInfo(new NodeStorage.StoreSortInfo(new DebugNodeTypeComparator(), ASC));
    tree.getNodeStorage()
        .addSortInfo(new NodeStorage.StoreSortInfo(new VariableNodeComparator(), ASC));
    watchExpressionPanel.addStyleName(resources.getCss().watchExpressionsPanel());
  }

  @Override
  public void removeAllVariables() {
    for (Node node : tree.getNodeStorage().getAll()) {
      if (node instanceof VariableNode) {
        tree.getNodeStorage().remove(node);
      }
    }
  }

  @Override
  public void setVariables(@NotNull List<? extends Variable> variables) {
    for (Variable variable : variables) {
      VariableNode node = nodeFactory.createVariableNode(variable);
      tree.getNodeStorage().add(node);
    }
  }

  @Override
  public void expandVariable(Variable variable) {
    String key = nodeKeyProvider.evaluateKey(variable);
    Node nodeToUpdate = tree.getNodeStorage().findNodeWithKey(key);
    if (nodeToUpdate != null) {
      tree.getNodeStorage().update(nodeToUpdate);
      List<? extends Variable> varChildren = variable.getValue().getVariables();
      for (int i = 0; i < varChildren.size(); i++) {
        Node childNode = nodeFactory.createVariableNode(varChildren.get(i));
        tree.getNodeStorage().insert(nodeToUpdate, i, childNode);
      }
    }
  }

  @Override
  public void updateVariable(Variable variable) {
    String key = nodeKeyProvider.evaluateKey(variable);
    Node nodeToUpdate = tree.getNodeStorage().findNodeWithKey(key);
    if (nodeToUpdate != null && nodeToUpdate instanceof VariableNode) {
      VariableNode variableNode = ((VariableNode) nodeToUpdate);
      variableNode.setData(variable);
      tree.getNodeStorage().update(variableNode);

      if (tree.isExpanded(nodeToUpdate)) {
        tree.getNodeLoader().loadChildren(variableNode);
      } else {
        tree.refresh(nodeToUpdate);
      }
    }
  }

  @Override
  public void addExpression(WatchExpression expression) {
    String key = nodeKeyProvider.evaluateKey(expression);
    if (tree.getNodeStorage().findNodeWithKey(key) == null) {
      WatchExpressionNode node = nodeFactory.createExpressionNode(expression);
      tree.getNodeStorage().add(node);
    }
  }

  @Override
  public void updateExpression(WatchExpression expression) {
    String key = nodeKeyProvider.evaluateKey(expression);
    Node nodeToUpdate = tree.getNodeStorage().findNodeWithKey(key);
    if (nodeToUpdate != null && nodeToUpdate instanceof WatchExpressionNode) {
      WatchExpressionNode expNode = ((WatchExpressionNode) nodeToUpdate);
      expNode.setData(expression);
      tree.getNodeStorage().update(nodeToUpdate);
      tree.refresh(nodeToUpdate);
    }
  }

  @Override
  public void removeExpression(WatchExpression expression) {
    String key = nodeKeyProvider.evaluateKey(expression);
    Node nodeToRemove = tree.getNodeStorage().findNodeWithKey(key);
    if (nodeToRemove != null) {
      tree.getNodeStorage().remove(nodeToRemove);
    }
  }

  @Override
  public Variable getSelectedVariable() {
    Node selectedNode = getSelectedNode();
    if (selectedNode instanceof VariableNode) {
      return ((VariableNode) selectedNode).getData();
    }
    return null;
  }

  @Override
  public WatchExpression getSelectedExpression() {
    Node selectedNode = getSelectedNode();
    if (selectedNode instanceof WatchExpressionNode) {
      return ((WatchExpressionNode) selectedNode).getData();
    }
    return null;
  }

  private Node getSelectedNode() {
    if (tree.getSelectionModel().getSelectedNodes().isEmpty()) {
      return null;
    }
    return tree.getSelectionModel().getSelectedNodes().get(0);
  }

  @Override
  public AcceptsOneWidget getDebuggerWatchToolbarPanel() {
    return watchExpressionPanel;
  }
}
