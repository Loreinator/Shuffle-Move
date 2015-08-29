/*  ShuffleMove - A program for identifying and simulating ideal moves in the game
 *  called Pokemon Shuffle.
 *  
 *  Copyright (C) 2015  Andrew Meyers
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package shuffle.fwk.service.editspecies;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.config.EntryType;
import shuffle.fwk.config.manager.ImageManager;
import shuffle.fwk.config.manager.SpeciesManager;
import shuffle.fwk.data.Effect;
import shuffle.fwk.data.PkmType;
import shuffle.fwk.data.Species;
import shuffle.fwk.gui.EffectChooser;
import shuffle.fwk.gui.PressOrClickMouseAdapter;
import shuffle.fwk.gui.TypeChooser;
import shuffle.fwk.gui.WrapLayout;
import shuffle.fwk.i18n.I18nUser;
import shuffle.fwk.service.BaseService;
import shuffle.fwk.service.DisposeAction;

/**
 * @author Andrew Meyers
 *
 */
public class EditSpeciesService extends BaseService<EditSpeciesServiceUser> implements I18nUser {
   
   // i18n keys
   private static final String KEY_NAME = "text.name";
   private static final String KEY_ATTACK = "text.attack";
   private static final String KEY_TYPE = "text.type";
   private static final String KEY_EFFECT = "text.effect";
   private static final String KEY_ICON = "text.icon";
   private static final String KEY_MEGA = "text.mega";
   private static final String KEY_OK = "button.ok";
   private static final String KEY_APPLY = "button.apply";
   private static final String KEY_CANCEL = "button.cancel";
   private static final String KEY_NEW = "button.new";
   private static final String KEY_SET = "button.set";
   private static final String KEY_FIND_ICON_TITLE = "text.findicon.title";
   private static final String KEY_FILTER_DESCRIPTION = "text.filter.desc";
   
   // Config keys
   public static final String KEY_ROSTER_CELL_OUTLINE_THICK = "ROSTER_CELL_OUTLINE_THICK";
   public static final String KEY_ROSTER_CELL_BORDER_THICK = "ROSTER_CELL_BORDER_THICK";
   public static final String KEY_ROSTER_CELL_MARGIN_THICK = "ROSTER_CELL_MARGIN_THICK";
   
   // Defaults
   public static final int DEFAULT_BORDER_WIDTH = 1;
   public static final int DEFAULT_BORDER_OUTLINE = 1;
   public static final int DEFAULT_BORDER_MARGIN = 1;
   
   // interface components
   private JDialog d = null;
   private JTextField nameField = null;
   private JComboBox<Integer> attackComboBox = null;
   private TypeChooser typeChooser = null;
   private EffectChooser effectChooser = null;
   private JTextField megaNameField = null;
   private EffectChooser megaEffectChooser = null;
   private JButton megaIconButton = null;
   private JButton setSpeciesButton = null;
   private JPanel speciesSelectPanel = null;
   private JFileChooser chooser = null;
   
   // Data and state variables
   private SpeciesManager speciesData = null;
   private ImageManager imageData = null;
   private Species selectedSpecies = null;
   private JPanel selectedComponent = null;
   private File selectedMainIconFile = null;
   private File selectedMegaIconFile = null;
   
   private String prevPath = null;
   
   private RevalidateListener listener = null;
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#getUserClass()
    */
   @Override
   protected Class<EditSpeciesServiceUser> getUserClass() {
      return EditSpeciesServiceUser.class;
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#setupGUI()
    */
   @Override
   public void onSetupGUI() {
      Component top = makeTopPanel();
      Component center = makeCenterPanel();
      Component bottom = makeBottomPanel();
      
      d = new JDialog(getOwner());
      d.setLayout(new BorderLayout());
      
      d.add(top, BorderLayout.NORTH);
      d.add(center, BorderLayout.CENTER);
      d.add(bottom, BorderLayout.SOUTH);
      
      ConfigManager preferencesManager = getUser().getPreferencesManager();
      final int width = preferencesManager.getIntegerValue(KEY_POPUP_WIDTH, DEFAULT_POPUP_WIDTH);
      final int height = preferencesManager.getIntegerValue(KEY_POPUP_HEIGHT, DEFAULT_POPUP_HEIGHT);
      
      addListeners();
      
      d.repaint();
      d.pack();
      d.setSize(width, height);
      d.setLocationRelativeTo(null);
      d.setResizable(false);
      setDialog(d);
      
      setupChooser();
   }
   
   private void setupChooser() {
      chooser = new JFileChooser(prevPath);
      chooser.setDialogTitle(getString(KEY_FIND_ICON_TITLE));
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setAcceptAllFileFilterUsed(false);
      chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
      chooser.setFileFilter(new FileFilter() {
         @Override
         public String getDescription() {
            return getString(KEY_FILTER_DESCRIPTION);
         }
         
         @Override
         public boolean accept(File f) {
            return f.isDirectory() || f.getPath().endsWith(".png");
         }
      });
   }
   
   private void addListeners() {
      nameField.getDocument().addDocumentListener(listener);
      attackComboBox.addItemListener(listener);
      typeChooser.addItemListener(listener);
      effectChooser.addItemListener(listener);
      megaNameField.getDocument().addDocumentListener(listener);
      megaEffectChooser.addItemListener(listener);
   }
   
   private void removeListeners() {
      nameField.getDocument().removeDocumentListener(listener);
      attackComboBox.removeItemListener(listener);
      typeChooser.removeItemListener(listener);
      effectChooser.removeItemListener(listener);
      megaNameField.getDocument().removeDocumentListener(listener);
      megaEffectChooser.removeItemListener(listener);
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#updateGUIFrom(java.lang.Object)
    */
   @Override
   protected void updateGUIFrom(EditSpeciesServiceUser user) {
      speciesData = new SpeciesManager(user.getSpeciesManager());
      imageData = new ImageManager(user.getImageManager());
      updateSpeciesSelectPanel();
      revalidateEntryFields();
   }
   
   protected void onOk() {
      onApply();
      dispose();
   }
   
   protected void onApply() {
      getUser().loadSpeciesManagerFrom(speciesData);
      getUser().loadImageManagerFrom(imageData);
   }
   
   private Component makeTopPanel() {
      listener = new RevalidateListener(new Runnable() {
         @Override
         public void run() {
            revalidateEntryFields();
         }
      });
      JPanel firstRow = new JPanel(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.NONE;
      c.weightx = 0.0;
      c.weighty = 0.0;
      c.gridx = 1;
      c.gridwidth = 1;
      c.gridheight = 1;
      
      c.weightx = 0.0;
      c.gridx++;
      JLabel nameLabel = new JLabel(getString(KEY_NAME));
      firstRow.add(nameLabel, c);
      
      c.fill = GridBagConstraints.BOTH;
      c.weightx = 1.0;
      c.gridx++;
      nameField = new JTextField() {
         private static final long serialVersionUID = -2685366060652361084L;
         
         @Override
         public Dimension getMinimumSize() {
            Dimension d = super.getMinimumSize();
            d.width = Math.max(50, d.width);
            return d;
         }
      };
      firstRow.add(nameField, c);
      c.weightx = 0.0;
      c.fill = GridBagConstraints.NONE;
      
      c.gridx++;
      JButton mainIconButton = new JButton(new AbstractAction(getString(KEY_ICON)) {
         private static final long serialVersionUID = -879845512314653662L;
         
         @Override
         public void actionPerformed(ActionEvent e) {
            selectMainIcon();
         }
      });
      firstRow.add(mainIconButton, c);
      
      c.gridx++;
      JLabel attackLabel = new JLabel(getString(KEY_ATTACK));
      firstRow.add(attackLabel, c);
      
      c.gridx++;
      attackComboBox = new JComboBox<Integer>();
      ConfigManager manager = getUser().getPreferencesManager();
      int attackStart = manager.getIntegerValue("ATTACK_CHOSER_START", 30);
      int attackEnd = manager.getIntegerValue("ATTACK_CHOSER_END", 80);
      for (int i = attackStart; i <= attackEnd; i += 10) {
         attackComboBox.addItem(i);
      }
      firstRow.add(attackComboBox, c);
      
      c.gridx++;
      JLabel typeLabel = new JLabel(getString(KEY_TYPE));
      firstRow.add(typeLabel, c);
      
      c.gridx++;
      typeChooser = new TypeChooser(false);
      firstRow.add(typeChooser, c);
      
      c.gridx++;
      JLabel effectLabel = new JLabel(getString(KEY_EFFECT));
      firstRow.add(effectLabel, c);
      
      c.gridx++;
      effectChooser = new EffectChooser(false, EffectChooser.DefaultEntry.NONE);
      firstRow.add(effectChooser, c);
      
      JPanel secondRow = new JPanel(new GridBagLayout());
      c.gridx = 1;
      
      c.gridx++;
      JButton newSpeciesButton = new JButton(new AbstractAction(getString(KEY_NEW)) {
         private static final long serialVersionUID = 7547320607139589184L;
         
         @Override
         public void actionPerformed(ActionEvent arg0) {
            setSelected(null, null);
         }
      });
      secondRow.add(newSpeciesButton);
      
      c.gridx++;
      setSpeciesButton = new JButton(new AbstractAction(getString(KEY_SET)) {
         private static final long serialVersionUID = 7547320607139589184L;
         
         @Override
         public void actionPerformed(ActionEvent arg0) {
            applySelectedSpecies();
         }
      });
      secondRow.add(setSpeciesButton);
      
      c.gridx++;
      JLabel megaLabel = new JLabel(getString(KEY_MEGA));
      secondRow.add(megaLabel, c);
      
      c.fill = GridBagConstraints.BOTH;
      c.weightx = 1.0;
      c.gridx++;
      megaNameField = new JTextField() {
         private static final long serialVersionUID = 4045938384142965286L;
         
         @Override
         public Dimension getMinimumSize() {
            Dimension d = super.getMinimumSize();
            d.width = Math.max(50, d.width);
            return d;
         }
      };
      secondRow.add(megaNameField, c);
      c.weightx = 0.0;
      c.fill = GridBagConstraints.NONE;
      
      c.gridx++;
      megaIconButton = new JButton(new AbstractAction(getString(KEY_ICON)) {
         private static final long serialVersionUID = -879845512314653662L;
         
         @Override
         public void actionPerformed(ActionEvent e) {
            selectMegaIcon();
         }
      });
      secondRow.add(megaIconButton, c);
      
      c.gridx++;
      JLabel megaEffectLabel = new JLabel(getString(KEY_EFFECT));
      secondRow.add(megaEffectLabel, c);
      
      c.gridx++;
      megaEffectChooser = new EffectChooser(true, EffectChooser.DefaultEntry.NONE);
      secondRow.add(megaEffectChooser, c);
      
      JPanel ret = new JPanel(new GridBagLayout());
      c.fill = GridBagConstraints.BOTH;
      c.weightx = 1.0;
      c.gridx = 1;
      c.gridy = 1;
      ret.add(firstRow, c);
      c.gridy = 2;
      ret.add(secondRow, c);
      return ret;
   }
   
   private Component makeCenterPanel() {
      speciesSelectPanel = new JPanel(new WrapLayout()) {
         private static final long serialVersionUID = 5094314715314389100L;
         
         // Fix to make it play nice with the scroll bar.
         @Override
         public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width = (int) (d.getWidth() - 20);
            return d;
         }
      };
      final JScrollPane ret = new JScrollPane(speciesSelectPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      ret.addComponentListener(new ComponentAdapter() {
         @Override
         public void componentResized(ComponentEvent e) {
            ret.revalidate();
         }
      });
      ret.getVerticalScrollBar().setUnitIncrement(27);
      return ret;
   }
   
   private Component makeBottomPanel() {
      JPanel ret = new JPanel(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.NONE;
      c.weightx = 1.0;
      c.weighty = 0.0;
      c.gridx = 1;
      c.gridy = 1;
      c.gridwidth = 1;
      c.gridheight = 1;
      
      c.insets = new Insets(0, 10, 0, 10);
      c.anchor = GridBagConstraints.LINE_END;
      c.weightx = 1.0;
      c.gridx += 1;
      JButton okButton = new JButton(getString(KEY_OK));
      ret.add(okButton, c);
      
      c.anchor = GridBagConstraints.CENTER;
      c.weightx = 0.0;
      c.gridx += 1;
      JButton applyButton = new JButton(getString(KEY_APPLY));
      ret.add(applyButton, c);
      
      c.anchor = GridBagConstraints.LINE_START;
      c.weightx = 0.0;
      c.gridx += 1;
      JButton cancelButton = new JButton(new DisposeAction(getString(KEY_CANCEL), this));
      ret.add(cancelButton, c);
      
      okButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            onOk();
         }
      });
      applyButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            onApply();
         }
      });
      return ret;
   }
   
   private void updateSpeciesSelectPanel() {
      speciesSelectPanel.removeAll();
      
      Species newSpecies = null;
      JPanel newComponent = null;
      Collection<Species> speciesValues = speciesData.getAllSpecies();
      ConfigManager manager = getUser().getPreferencesManager();
      int borderThick = manager.getIntegerValue(KEY_ROSTER_CELL_BORDER_THICK, DEFAULT_BORDER_WIDTH);
      int outlineThick = manager.getIntegerValue(KEY_ROSTER_CELL_OUTLINE_THICK, DEFAULT_BORDER_OUTLINE);
      int marginThick = manager.getIntegerValue(KEY_ROSTER_CELL_MARGIN_THICK, DEFAULT_BORDER_MARGIN);
      for (Species s : speciesValues) {
         JPanel component = createRosterComponent(s);
         if (s.equals(selectedSpecies)) {
            newSpecies = s;
            newComponent = component;
            setBorderFor(component, true, borderThick, marginThick, outlineThick);
         } else {
            setBorderFor(component, false, borderThick, marginThick, outlineThick);
         }
         speciesSelectPanel.add(component);
      }
      setSelected(newSpecies, newComponent);
      speciesSelectPanel.revalidate();
   }
   
   private void setSelected(Species species, JPanel newComponent) {
      selectedSpecies = species;
      ConfigManager manager = getUser().getPreferencesManager();
      int borderThick = manager.getIntegerValue(KEY_ROSTER_CELL_BORDER_THICK, DEFAULT_BORDER_WIDTH);
      int outlineThick = manager.getIntegerValue(KEY_ROSTER_CELL_OUTLINE_THICK, DEFAULT_BORDER_OUTLINE);
      int marginThick = manager.getIntegerValue(KEY_ROSTER_CELL_MARGIN_THICK, DEFAULT_BORDER_MARGIN);
      if (selectedComponent != null) {
         setBorderFor(selectedComponent, false, borderThick, marginThick, outlineThick);
      }
      selectedComponent = newComponent;
      setBorderFor(selectedComponent, true, borderThick, marginThick, outlineThick);
      setDisplayedSpecies(species);
   }
   
   private void setBorderFor(JPanel c, boolean doBorder, int borderThick, int marginThick, int outlineThick) {
      if (c != null) {
         Border main;
         Border margin = new EmptyBorder(marginThick, marginThick, marginThick, marginThick);
         if (doBorder) {
            main = new LineBorder(Color.BLACK, borderThick);
         } else {
            main = new EmptyBorder(borderThick, borderThick, borderThick, borderThick);
         }
         Border greyOutline = new LineBorder(Color.gray, outlineThick);
         Border innerChunk = BorderFactory.createCompoundBorder(greyOutline, margin);
         Border outerChunk = BorderFactory.createCompoundBorder(main, margin);
         Border finalBorder = BorderFactory.createCompoundBorder(outerChunk, innerChunk);
         c.setBorder(finalBorder);
      }
   }
   
   private JPanel createRosterComponent(Species s) {
      JPanel ret = new JPanel(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.BOTH;
      c.gridx = 1;
      c.gridy = 1;
      c.anchor = GridBagConstraints.CENTER;
      
      MouseAdapter ma = new PressOrClickMouseAdapter() {
         
         @Override
         protected void onRight(MouseEvent e) {
            onLeft(e);
         }
         
         @Override
         protected void onLeft(MouseEvent e) {
            setSelected(s, ret);
         }
         
         @Override
         protected void onEnter() {
            // Do nothing
         }
      };
      
      ImageIcon icon = imageData.getImageFor(s);
      JLabel iconLabel = new JLabel(icon);
      iconLabel.addMouseListener(ma);
      ret.add(iconLabel, c);
      c.gridy += 1;
      String text = s.getLocalizedName();
      JLabel jLabel = new JLabel(text);
      jLabel.setHorizontalTextPosition(SwingConstants.CENTER);
      jLabel.setHorizontalAlignment(SwingConstants.CENTER);
      jLabel.addMouseListener(ma);
      ret.add(jLabel, c);
      
      return ret;
   }
   
   private void revalidateEntryFields() {
      String name = nameField.getText();
      PkmType type = typeChooser.getSelectedType();
      int attackIndex = attackComboBox.getSelectedIndex();
      Integer attack = attackComboBox.getItemAt(attackIndex);
      Effect mainEffect = effectChooser.getSelectedEffect();
      String megaName = megaNameField.getText();
      if (name != null && !name.isEmpty() && type != null && attack != null && attack >= 0 && mainEffect != null) {
         // basic data is valid.
         setSpeciesButton.setEnabled(true);
      } else {
         // basic data fails.
         setSpeciesButton.setEnabled(false);
      }
      boolean allowModification = mainEffect != null && mainEffect.canLevel();
      nameField.setEnabled(selectedSpecies == null);
      attackComboBox.setEnabled(allowModification);
      typeChooser.setEnabled(allowModification);
      effectChooser.setEnabled(allowModification);
      megaNameField.setEnabled(allowModification && name != null && !name.isEmpty());
      megaEffectChooser.setEnabled(allowModification && megaName != null && !megaName.trim().isEmpty());
      megaIconButton.setEnabled(allowModification && megaName != null && !megaName.trim().isEmpty());
   }
   
   private void setDisplayedSpecies(Species species) {
      removeListeners();
      String name;
      PkmType type;
      Integer attack;
      Effect mainEffect;
      String megaName;
      Effect megaEffect;
      if (species == null) {
         name = "";
         type = PkmType.BUG;
         ConfigManager manager = getUser().getPreferencesManager();
         int attackStart = manager.getIntegerValue("ATTACK_CHOSER_START", 30);
         attack = attackStart;
         mainEffect = Effect.NONE;
         megaName = null;
         megaEffect = Effect.NONE;
      } else {
         name = species.getName();
         type = species.getType();
         attack = species.getBaseAttack();
         mainEffect = species.getEffect();
         megaName = species.getMegaName();
         megaEffect = species.getMegaEffect();
      }
      nameField.setText(name);
      typeChooser.setSelectedType(type);
      DefaultComboBoxModel<Integer> attackModel = (DefaultComboBoxModel<Integer>) attackComboBox.getModel();
      if (attackModel.getIndexOf(attack) == -1) {
         attackComboBox.insertItemAt(attack, 0);
      }
      attackComboBox.setSelectedItem(attack);
      effectChooser.setSelectedEffect(mainEffect);
      if (megaName != null) {
         megaName = megaName.trim().replaceAll("_", " ");
      }
      megaNameField.setText(megaName);
      megaEffectChooser.setSelectedEffect(megaEffect);
      
      selectedMainIconFile = imageData.getFileValue(name);
      selectedMegaIconFile = imageData.getFileValue(megaName);
      
      revalidateEntryFields();
      addListeners();
   }
   
   private void applySelectedSpecies() {
      String name = nameField.getText();
      int number = selectedSpecies == null ? speciesData.getNextNumber() : selectedSpecies.getNumber();
      PkmType type = typeChooser.getSelectedType();
      Integer attack = attackComboBox.getItemAt(attackComboBox.getSelectedIndex());
      Effect mainEffect = effectChooser.getSelectedEffect();
      String megaName = megaNameField.getText();
      Effect megaEffect = megaEffectChooser.getSelectedEffect();
      if (name != null && !name.isEmpty() && type != null && attack != null && attack >= 0 && mainEffect != null) {
         // todo transfer index
         if (megaName != null) {
            megaName = megaName.trim().replaceAll("\\s+", "_");
         }
         Species toSet = new Species(name, number, attack, type, mainEffect, megaName, megaEffect);
         String prevName = selectedSpecies == null ? null : selectedSpecies.getName();
         String prevMegaName = selectedSpecies == null ? null : selectedSpecies.getMegaName();
         boolean removeOld = false;
         if (speciesData.hasKey(EntryType.SPECIES, prevName)) {
            removeOld |= speciesData.removeEntry(EntryType.SPECIES, prevName);
         }
         speciesData.setEntry(EntryType.SPECIES, toSet.getName(), toSet);
         if (removeOld && imageData.hasKey(EntryType.FILE, prevName)) {
            imageData.removeEntry(EntryType.FILE, prevName);
         }
         if (removeOld && imageData.hasKey(EntryType.FILE, prevMegaName)) {
            imageData.removeEntry(EntryType.FILE, prevMegaName);
         }
         imageData.setEntry(EntryType.FILE, name, selectedMainIconFile);
         imageData.setEntry(EntryType.FILE, megaName, selectedMegaIconFile);
         imageData.loadIcons(Arrays.asList(name, megaName, prevName, prevMegaName));
         selectedSpecies = toSet;
      }
      updateSpeciesSelectPanel();
   }
   
   private void selectMainIcon() {
      selectedMainIconFile = launchLocationChooser();
   }
   
   private void selectMegaIcon() {
      selectedMegaIconFile = launchLocationChooser();
   }
   
   /**
    * @return
    */
   private File launchLocationChooser() {
      File ret;
      if (chooser.showOpenDialog(getOwner()) == JFileChooser.APPROVE_OPTION) {
         ret = chooser.getSelectedFile();
         String curDir = System.getProperty("user.dir");
         if (curDir != null && ret != null) {
            String path = ret.getAbsolutePath();
            if (path.startsWith(curDir)) {
               String relativePath = path.substring(curDir.length() + 1);
               ret = new File(relativePath);
            }
         }
      } else {
         ret = null;
      }
      prevPath = chooser.getCurrentDirectory().getAbsolutePath();
      return ret;
   }
   
   private static class RevalidateListener implements DocumentListener, ItemListener {
      
      private final Runnable runThis;
      
      public RevalidateListener(Runnable runThis) {
         this.runThis = runThis;
      }
      
      @Override
      public void removeUpdate(DocumentEvent e) {
         changedUpdate(e);
      }
      
      @Override
      public void insertUpdate(DocumentEvent e) {
         changedUpdate(e);
      }
      
      @Override
      public void changedUpdate(DocumentEvent e) {
         runThis.run();
      }
      
      /*
       * (non-Javadoc)
       * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
       */
      @Override
      public void itemStateChanged(ItemEvent arg0) {
         runThis.run();
      }
   }
}
