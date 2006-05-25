/*
 * SweetHome3DSwingDraft.java 5 d�c. 2005
 * 
 * Copyright (c) 2005 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights
 * Reserved.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.sweethome3d.draft;

import java.awt.BorderLayout;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Draft application for Sweet Home 3D GUI using Swing.
 * @author Emmanuel Puybaret
 */
public class SweetHome3DSwingDraft extends JFrame {
  // Default components generated by Visual Editor for applications
  private JPanel      frameContentPane    = null;
  private JMenuBar    frameMenuBar        = null;
  private JMenu       fileMenu            = null;
  private JMenu       editMenu            = null;
  private JMenu       helpMenu            = null;
  private JMenuItem   exitMenuItem        = null;
  private JMenuItem   aboutMenuItem       = null;
  private JMenuItem   cutMenuItem         = null;
  private JMenuItem   copyMenuItem        = null;
  private JMenuItem   pasteMenuItem       = null;
  private JMenuItem   saveMenuItem        = null;
  // Sweet Home 3D draft additional components
  private JSplitPane  mainSplitPane       = null;
  private JSplitPane  leftSplitPane       = null;
  private JSplitPane  rightSplitPane      = null;
  private JToolBar    toolBar             = null;
  private JScrollPane planScrollPane      = null;
  private JScrollPane view3DScrollPane    = null;
  private JScrollPane catalogScrollPane   = null;
  private JScrollPane furnitureScrollPane = null;
  private JButton     cutButton           = null;
  private JButton     copyButton          = null;
  private JButton     pasteButton         = null;
  private JTree       catalogTree         = null;
  private JTable      furnitureTable      = null;
  private JLabel      planLabel           = null;
  private JLabel      view3DLabel         = null;
  private JMenu       furnitureMenu       = null;
  private JMenu       planMenu            = null;
  private JMenuItem   newMenuItem         = null;
  private JMenuItem   openMenuItem        = null;
  private JMenuItem   saveAsMenuItem      = null;
  private JMenuItem   preferencesMenuItem = null;
  private JMenuItem   undoMenuItem        = null;
  private JMenuItem   redoMenuItem        = null;
  private JMenuItem   addMenuItem         = null;
  private JMenuItem   deleteMenuItem      = null;
  private JMenuItem   importImageMenuItem = null;
  private JMenuItem   showRulesMenuItem   = null;

  /**
   * This is the default constructor.
   */
  public SweetHome3DSwingDraft() {
    super();
    initialize();
  }

  /**
   * This method initializes this frame.
   */
  private void initialize() {
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setJMenuBar(getFrameMenuBar());
    this.setSize(300, 200);
    this.setContentPane(getFrameContentPane());
    this.setTitle("Sweet Home 3D");
  }

  /**
   * This method initializes frameContentPane
   */
  private JPanel getFrameContentPane() {
    if (frameContentPane == null) {
      frameContentPane = new JPanel();
      frameContentPane.setLayout(new BorderLayout());
      frameContentPane.add(getMainSplitPane(),
          java.awt.BorderLayout.CENTER);
      frameContentPane.add(getToolBar(),
          java.awt.BorderLayout.NORTH);
    }
    return frameContentPane;
  }

  /**
   * This method initializes frameMenuBar.
   */
  private JMenuBar getFrameMenuBar() {
    if (frameMenuBar == null) {
      frameMenuBar = new JMenuBar();
      frameMenuBar.add(getFileMenu());
      frameMenuBar.add(getEditMenu());
      frameMenuBar.add(getFurnitureMenu());
      frameMenuBar.add(getPlanMenu());
      frameMenuBar.add(getHelpMenu());
    }
    return frameMenuBar;
  }

  /**
   * This method initializes fileMenu.
   */
  private JMenu getFileMenu() {
    if (fileMenu == null) {
      fileMenu = new JMenu();
      fileMenu.setText("File");
      fileMenu.add(getNewMenuItem());
      fileMenu.add(getOpenMenuItem());
      fileMenu.add(getSaveMenuItem());
      fileMenu.add(getSaveAsMenuItem());
      fileMenu.add(getPreferencesMenuItem());
      fileMenu.add(getExitMenuItem());
    }
    return fileMenu;
  }

  /**
   * This method initializes editMenu.
   */
  private JMenu getEditMenu() {
    if (editMenu == null) {
      editMenu = new JMenu();
      editMenu.setText("Edit");
      editMenu.add(getUndoMenuItem());
      editMenu.add(getRedoMenuItem());
      editMenu.add(getCutMenuItem());
      editMenu.add(getCopyMenuItem());
      editMenu.add(getPasteMenuItem());
    }
    return editMenu;
  }

  /**
   * This method initializes furnitureMenu.
   */
  private JMenu getFurnitureMenu() {
    if (furnitureMenu == null) {
      furnitureMenu = new JMenu();
      furnitureMenu.setText("Furniture");
      furnitureMenu.add(getAddMenuItem());
      furnitureMenu.add(getDeleteMenuItem());
    }
    return furnitureMenu;
  }

  /**
   * This method initializes planMenu.
   */
  private JMenu getPlanMenu() {
    if (planMenu == null) {
      planMenu = new JMenu();
      planMenu.setText("Plan");
      planMenu.add(getImportImageMenuItem());
      planMenu.add(getShowRulesMenuItem());
    }
    return planMenu;
  }

  /**
   * This method initializes helpMenu.
   */
  private JMenu getHelpMenu() {
    if (helpMenu == null) {
      helpMenu = new JMenu();
      helpMenu.setText("Help");
      helpMenu.add(getAboutMenuItem());
    }
    return helpMenu;
  }

  /**
   * This method initializes newMenuItem.
   */
  private JMenuItem getNewMenuItem() {
    if (newMenuItem == null) {
      newMenuItem = new JMenuItem();
      newMenuItem.setText("New");
    }
    return newMenuItem;
  }

  /**
   * This method initializes openMenuItem.
   */
  private JMenuItem getOpenMenuItem() {
    if (openMenuItem == null) {
      openMenuItem = new JMenuItem();
      openMenuItem.setText("Open...");
    }
    return openMenuItem;
  }

  /**
   * This method initializes saveMenuItem.
   */
  private JMenuItem getSaveMenuItem() {
    if (saveMenuItem == null) {
      saveMenuItem = new JMenuItem();
      saveMenuItem.setText("Save");
      saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(
          KeyEvent.VK_S, Event.CTRL_MASK, true));
    }
    return saveMenuItem;
  }

  /**
   * This method initializes saveAsMenuItem.
   */
  private JMenuItem getSaveAsMenuItem() {
    if (saveAsMenuItem == null) {
      saveAsMenuItem = new JMenuItem();
      saveAsMenuItem.setText("Save as...");
    }
    return saveAsMenuItem;
  }

  /**
   * This method initializes preferencesMenuItem.
   */
  private JMenuItem getPreferencesMenuItem() {
    if (preferencesMenuItem == null) {
      preferencesMenuItem = new JMenuItem();
      preferencesMenuItem.setText("Preferences...");
    }
    return preferencesMenuItem;
  }

  /**
   * This method initializes exitMenuItem.
   */
  private JMenuItem getExitMenuItem() {
    if (exitMenuItem == null) {
      exitMenuItem = new JMenuItem();
      exitMenuItem.setText("Exit");
      exitMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          System.exit(0);
        }
      });
    }
    return exitMenuItem;
  }

  /**
   * This method initializes undoMenuItem.
   */
  private JMenuItem getUndoMenuItem() {
    if (undoMenuItem == null) {
      undoMenuItem = new JMenuItem();
      undoMenuItem.setText("Undo");
    }
    return undoMenuItem;
  }

  /**
   * This method initializes redoMenuItem.
   */
  private JMenuItem getRedoMenuItem() {
    if (redoMenuItem == null) {
      redoMenuItem = new JMenuItem();
      redoMenuItem.setText("Redo");
    }
    return redoMenuItem;
  }

  /**
   * This method initializes cutMenuItem.
   */
  private JMenuItem getCutMenuItem() {
    if (cutMenuItem == null) {
      cutMenuItem = new JMenuItem();
      cutMenuItem.setText("Cut");
      cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(
          KeyEvent.VK_X, Event.CTRL_MASK, true));
    }
    return cutMenuItem;
  }

  /**
   * This method initializes copyMenuItem.
   */
  private JMenuItem getCopyMenuItem() {
    if (copyMenuItem == null) {
      copyMenuItem = new JMenuItem();
      copyMenuItem.setText("Copy");
      copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(
          KeyEvent.VK_C, Event.CTRL_MASK, true));
    }
    return copyMenuItem;
  }

  /**
   * This method initializes pasteMenuItem.
   */
  private JMenuItem getPasteMenuItem() {
    if (pasteMenuItem == null) {
      pasteMenuItem = new JMenuItem();
      pasteMenuItem.setText("Paste");
      pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(
          KeyEvent.VK_V, Event.CTRL_MASK, true));
    }
    return pasteMenuItem;
  }

  /**
   * This method initializes addMenuItem.
   */
  private JMenuItem getAddMenuItem() {
    if (addMenuItem == null) {
      addMenuItem = new JMenuItem();
      addMenuItem.setText("Add");
    }
    return addMenuItem;
  }

  /**
   * This method initializes deleteMenuItem.
   */
  private JMenuItem getDeleteMenuItem() {
    if (deleteMenuItem == null) {
      deleteMenuItem = new JMenuItem();
      deleteMenuItem.setText("Delete");
    }
    return deleteMenuItem;
  }

  /**
   * This method initializes importImageMenuItem.
   */
  private JMenuItem getImportImageMenuItem() {
    if (importImageMenuItem == null) {
      importImageMenuItem = new JMenuItem();
      importImageMenuItem.setText("Import image...");
    }
    return importImageMenuItem;
  }

  /**
   * This method initializes showRulesMenuItem.
   */
  private JMenuItem getShowRulesMenuItem() {
    if (showRulesMenuItem == null) {
      showRulesMenuItem = new JMenuItem();
      showRulesMenuItem.setText("Show rules");
    }
    return showRulesMenuItem;
  }

  /**
   * This method initializes aboutMenuItem.
   */
  private JMenuItem getAboutMenuItem() {
    if (aboutMenuItem == null) {
      aboutMenuItem = new JMenuItem();
      aboutMenuItem.setText("About");
      aboutMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JOptionPane.showMessageDialog(
              SweetHome3DSwingDraft.this,
              "Sweet Home 3D Draft\n� Copyrights 2006 eTeks",
              "About", JOptionPane.PLAIN_MESSAGE);
        }
      });
    }
    return aboutMenuItem;
  }

  /**
   * This method initializes toolBar.
   */
  private JToolBar getToolBar() {
    if (toolBar == null) {
      toolBar = new JToolBar();
      toolBar.add(getCutButton());
      toolBar.add(getCopyButton());
      toolBar.add(getPasteButton());
    }
    return toolBar;
  }

  /**
   * This method initializes cutButton.
   */
  private JButton getCutButton() {
    if (cutButton == null) {
      cutButton = new JButton();
      cutButton.setIcon(new ImageIcon(getClass().getResource(
          "/com/eteks/sweethome3d/draft/resources/Cut16.gif")));
    }
    return cutButton;
  }

  /**
   * This method initializes copyButton.
   */
  private JButton getCopyButton() {
    if (copyButton == null) {
      copyButton = new JButton();
      copyButton.setIcon(new ImageIcon(getClass().getResource(
          "/com/eteks/sweethome3d/draft/resources/Copy16.gif")));
    }
    return copyButton;
  }

  /**
   * This method initializes pasteButton.
   */
  private JButton getPasteButton() {
    if (pasteButton == null) {
      pasteButton = new JButton();
      pasteButton.setIcon(new ImageIcon(getClass().getResource(
          "/com/eteks/sweethome3d/draft/resources/Paste16.gif")));
    }
    return pasteButton;
  }

  /**
   * This method initializes mainSplitPane.
   */
  private JSplitPane getMainSplitPane() {
    if (mainSplitPane == null) {
      mainSplitPane = new JSplitPane();
      mainSplitPane.setResizeWeight(0.3D);
      mainSplitPane.setLeftComponent(getLeftSplitPane());
      mainSplitPane.setRightComponent(getRightSplitPane());
    }
    return mainSplitPane;
  }

  /**
   * This method initializes leftSplitPane.
   */
  private JSplitPane getLeftSplitPane() {
    if (leftSplitPane == null) {
      leftSplitPane = new JSplitPane();
      leftSplitPane
          .setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
      leftSplitPane.setResizeWeight(0.5D);
      leftSplitPane.setBottomComponent(getFurnitureScrollPane());
      leftSplitPane
          .setTopComponent(getCatalogScrollPane());
    }
    return leftSplitPane;
  }

  /**
   * This method initializes rightSplitPane.
   */
  private JSplitPane getRightSplitPane() {
    if (rightSplitPane == null) {
      rightSplitPane = new JSplitPane();
      rightSplitPane
          .setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
      rightSplitPane.setResizeWeight(0.5D);
      rightSplitPane.setBottomComponent(getView3DScrollPane());
      rightSplitPane.setTopComponent(getPlanScrollPane());
    }
    return rightSplitPane;
  }

  /**
   * This method initializes planScrollPane.
   */
  private JScrollPane getPlanScrollPane() {
    if (planScrollPane == null) {
      planLabel = new JLabel();
      planLabel.setText("");
      planLabel.setIcon(new ImageIcon(getClass().getResource(
          "/com/eteks/sweethome3d/draft/resources/plan.png")));
      planLabel
          .setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      planScrollPane = new JScrollPane();
      planScrollPane.setViewportView(planLabel);
    }
    return planScrollPane;
  }

  /**
   * This method initializes view3DScrollPane.
   */
  private JScrollPane getView3DScrollPane() {
    if (view3DScrollPane == null) {
      view3DLabel = new JLabel();
      view3DLabel.setText("");
      view3DLabel.setIcon(new ImageIcon(getClass().getResource(
          "/com/eteks/sweethome3d/draft/resources/view3D.jpg")));
      view3DLabel
          .setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      view3DScrollPane = new JScrollPane();
      view3DScrollPane.setViewportView(view3DLabel);
    }
    return view3DScrollPane;
  }

  /**
   * This method initializes catalogScrollPane.
   */
  private JScrollPane getCatalogScrollPane() {
    if (catalogScrollPane == null) {
      catalogScrollPane = new JScrollPane();
      catalogScrollPane
          .setViewportView(getCatalogTree());
    }
    return catalogScrollPane;
  }

  /**
   * This method initializes furnitureScrollPane.
   */
  private JScrollPane getFurnitureScrollPane() {
    if (furnitureScrollPane == null) {
      furnitureScrollPane = new JScrollPane();
      furnitureScrollPane.setViewportView(getFurnitureTable());
    }
    return furnitureScrollPane;
  }

  /**
   * This method initializes catalogTree.
   */
  private JTree getCatalogTree() {
    if (catalogTree == null) {
      DefaultMutableTreeNode bedroom = new DefaultMutableTreeNode(
          "Bedroom");
      bedroom.add(new DefaultMutableTreeNode("Bed 140x190"));
      bedroom.add(new DefaultMutableTreeNode("Chest"));
      bedroom.add(new DefaultMutableTreeNode("Bedside table"));

      DefaultMutableTreeNode livingRoom = new DefaultMutableTreeNode(
          "Living Room");
      livingRoom.add(new DefaultMutableTreeNode("Bookcase"));
      livingRoom.add(new DefaultMutableTreeNode("Chair"));
      livingRoom.add(new DefaultMutableTreeNode("Round table"));

      DefaultMutableTreeNode furnitureRoot = new DefaultMutableTreeNode();
      furnitureRoot.add(bedroom);
      furnitureRoot.add(livingRoom);

      catalogTree = new JTree(furnitureRoot);
      catalogTree.setRootVisible(false);
      catalogTree.setShowsRootHandles(true);
    }
    return catalogTree;
  }

  /**
   * This method initializes furnitureTable.
   */
  private JTable getFurnitureTable() {
    if (furnitureTable == null) {
      Object [] columnsTitle = {"Name", "W", "P", "H"};
      Object [][] furnitureData = { {"Bed", 140, 190, 50},
          {"Chest", 100, 80, 80}, {"Table", 110, 110, 75},
          {"Chair", 45, 45, 90}, {"Bookcase", 90, 30, 180}};
      furnitureTable = new JTable(furnitureData, columnsTitle);
    }
    return furnitureTable;
  }

  /**
   * Launches this application.
   */
  public static void main(String [] args) {
    try {
      UIManager.setLookAndFeel(UIManager
          .getSystemLookAndFeelClassName());
    } catch (Exception ex) {
    }
    SweetHome3DSwingDraft application = new SweetHome3DSwingDraft();
    application.setSize(800, 700);
    application.show();
  }
}


