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

package shuffle.fwk.service.roster;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.apache.commons.lang3.text.WordUtils;

import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.config.EntryType;
import shuffle.fwk.config.manager.ImageManager;
import shuffle.fwk.config.manager.RosterManager;
import shuffle.fwk.config.manager.SpeciesManager;
import shuffle.fwk.data.Effect;
import shuffle.fwk.data.PkmType;
import shuffle.fwk.data.Species;
import shuffle.fwk.data.SpeciesPaint;
import shuffle.fwk.data.Stage;
import shuffle.fwk.gui.EffectChooser;
import shuffle.fwk.gui.MultiListener;
import shuffle.fwk.gui.PressOrClickMouseAdapter;
import shuffle.fwk.gui.TypeChooser;
import shuffle.fwk.gui.WrapLayout;
import shuffle.fwk.gui.user.MultiListenerUser;
import shuffle.fwk.i18n.I18nUser;
import shuffle.fwk.service.BaseService;
import shuffle.fwk.service.DisposeAction;

/**
 * @author Andrew Meyers
 *
 */
public class EditRosterService extends BaseService<EditRosterServiceUser> implements I18nUser, MultiListenerUser,
      Observer {
   
   private static final String KEY_NONE_SELECTED = "text.noneselected";
   private static final String KEY_SELECTED = "text.selected";
   private static final String KEY_OK = "button.ok";
   private static final String KEY_APPLY = "button.apply";
   private static final String KEY_CANCEL = "button.cancel";
   private static final String KEY_TITLE = "text.title";
   private static final String KEY_LEVEL = "text.level";
   private static final String KEY_NAME = "text.name";
   private static final String KEY_TYPE = "text.type";
   private static final String KEY_MEGA_FILTER = "text.megafilter";
   private static final String KEY_SET_FOR_ALL = "button.setforall";
   private static final String KEY_TEAM = "text.team";
   private static final String KEY_TEAM_TOOLTIP = "tooltip.team";
   private static final String KEY_SET_FOR_ALL_TOOLTIP = "tooltip.setforall";
   private static final String KEY_NAME_TOOLTIP = "tooltip.name";
   private static final String KEY_MEGA_FILTER_TOOLTIP = "tooltip.megafilter";
   private static final String KEY_TYPE_TOOLTIP = "tooltip.type";
   private static final String KEY_LEVEL_TOOLTIP = "tooltip.level";
   private static final String KEY_SELECTED_TOOLTIP = "tooltip.selected";
   private static final String KEY_CANDY_TOOLTIP = "tooltip.candy";
   private static final String KEY_SKILL_BOOSTER_TOOLTIP = "tooltip.skill";
   private static final String KEY_EFFECT_FILTER_TOOLTIP = "tooltip.effectfilter";
   private static final String KEY_POKEMON_LEVEL_TOOLTIP = "tooltip.specieslevel";
   private static final String KEY_ACTIVE_EFFECT = "tooltip.active.effect";
   private static final String KEY_OK_TOOLTIP = "tooltip.ok";
   private static final String KEY_APPLY_TOOLTIP = "tooltip.apply";
   private static final String KEY_CANCEL_TOOLTIP = "tooltip.cancel";
   
   public static final String KEY_ROSTER_CELL_OUTLINE_THICK = "ROSTER_CELL_OUTLINE_THICK";
   public static final String KEY_ROSTER_CELL_BORDER_THICK = "ROSTER_CELL_BORDER_THICK";
   public static final String KEY_ROSTER_CELL_MARGIN_THICK = "ROSTER_CELL_MARGIN_THICK";
   private static final String KEY_EDIT_ROSTER_WIDTH = "EDIT_ROSTER_WIDTH";
   private static final String KEY_EDIT_ROSTER_HEIGHT = "EDIT_ROSTER_HEIGHT";
   private static final String KEY_CANDY_ICON = ImageManager.KEY_CANDY;
   private static final String KEY_SKILL_BOOSTER = ImageManager.KEY_SKILL_BOOSTER;
   
   public static final ExecutorService EXECUTOR = new ScheduledThreadPoolExecutor(1);
   
   public static final int DEFAULT_BORDER_WIDTH = 1;
   public static final int DEFAULT_BORDER_OUTLINE = 1;
   public static final int DEFAULT_BORDER_MARGIN = 1;
   
   private TypeChooser typeChooser = null;
   private JLabel selectedDisplayLabel = null;
   private JPanel rosterEntryPanel = null;
   private JSpinner levelSpinner = null;
   private JTextField textField = null;
   private JCheckBox megaFilter = null;
   private EffectChooser effectFilter = null;
   private JDialog d = null;
   private JComboBox<Integer> speedups = null;
   private JComboBox<Integer> skillLevels = null;
   private EffectChooser activeEffect = null;
   private ItemListener speedupsListener = null;
   private ItemListener skillLevelsListener = null;
   private ItemListener activeEffectListener = null;
   private JCheckBox teamFilter = null;
   private Supplier<Dimension> getMinUpperPanel = null;
   
   private RosterManager myData = null;
   
   private JPanel selectedComponent = null;
   private Species selectedSpecies = null;
   private Collection<Species> teamSpecies = new ArrayList<Species>();
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#getUserClass()
    */
   @Override
   protected Class<EditRosterServiceUser> getUserClass() {
      return EditRosterServiceUser.class;
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#setupGUI()
    */
   @Override
   public void onSetupGUI() {
      d = new JDialog(getOwner(), getString(KEY_TITLE));
      d.setTitle(getString(KEY_TITLE));
      d.setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 1.0;
      c.weighty = 0.0;
      c.gridx = 1;
      c.gridy = 1;
      c.gridwidth = 1;
      c.gridheight = 1;
      d.add(makeUpperPanel(), c);
      
      c.gridy += 1;
      c.fill = GridBagConstraints.BOTH;
      c.weighty = 1.0;
      d.add(makeCenterPanel(), c);
      
      c.gridy += 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weighty = 0.0;
      d.add(makeBottomPanel(), c);
      
      ConfigManager preferencesManager = getUser().getPreferencesManager();
      int defaultWidth = preferencesManager.getIntegerValue(KEY_POPUP_WIDTH, DEFAULT_POPUP_WIDTH);
      int defaultHeight = preferencesManager.getIntegerValue(KEY_POPUP_HEIGHT, DEFAULT_POPUP_HEIGHT);
      int width = preferencesManager.getIntegerValue(KEY_EDIT_ROSTER_WIDTH, defaultWidth);
      int height = preferencesManager.getIntegerValue(KEY_EDIT_ROSTER_HEIGHT, defaultHeight);
      d.repaint();
      d.pack();
      d.setMinimumSize(new Dimension(getMinimumWidth(), DEFAULT_POPUP_HEIGHT));
      d.setSize(new Dimension(width, height));
      d.setLocationRelativeTo(null);
      d.setResizable(true);
      addActionListeners();
      setDialog(d);
      getUser().addObserver(this);
   }
   
   private int getMinimumWidth() {
      int ret = 0;
      if (getMinUpperPanel != null) {
         ret += Math.max(0, getMinUpperPanel.get().width);
      }
      return Math.max(ret, DEFAULT_POPUP_WIDTH);
   }
   
   @Override
   protected void onLaunch() {
      d.addComponentListener(new ComponentAdapter() {
         @Override
         public void componentResized(ComponentEvent ev) {
            ConfigManager preferencesManager = getUser().getPreferencesManager();
            Dimension dim = d.getSize();
            preferencesManager.setEntry(EntryType.INTEGER, KEY_EDIT_ROSTER_WIDTH, dim.width);
            preferencesManager.setEntry(EntryType.INTEGER, KEY_EDIT_ROSTER_HEIGHT, dim.height);
         }
      });
   }
   
   private void addActionListeners() {
      MultiListener listener = new MultiListener(this);
      typeChooser.addItemListener(listener);
      textField.getDocument().addDocumentListener(listener);
      levelSpinner.getModel().addChangeListener(listener);
      megaFilter.addItemListener(listener);
      teamFilter.addItemListener(listener);
      effectFilter.addItemListener(listener);
   }
   
   private Component makeUpperPanel() {
      JPanel ret = new JPanel(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 1.0;
      c.weighty = 0.0;
      c.gridx = 1;
      c.gridy = 1;
      c.gridwidth = 1;
      c.gridheight = 1;
      
      c.gridx += 1;
      c.weightx = 0.0;
      JPanel typePanel = new JPanel();
      typePanel.add(new JLabel(getString(KEY_TYPE)));
      typeChooser = new TypeChooser(true);
      typePanel.add(typeChooser);
      typePanel.setToolTipText(getString(KEY_TYPE_TOOLTIP));
      typeChooser.setToolTipText(getString(KEY_TYPE_TOOLTIP));
      ret.add(typePanel, c);
      
      c.gridx += 1;
      c.weightx = 0.0;
      JPanel levelPanel = new JPanel();
      levelPanel.add(new JLabel(getString(KEY_LEVEL)));
      SpinnerNumberModel snm = new SpinnerNumberModel(0, 0, Species.MAX_LEVEL, 1);
      levelSpinner = new JSpinner(snm);
      levelPanel.add(levelSpinner);
      levelPanel.setToolTipText(getString(KEY_LEVEL_TOOLTIP));
      levelSpinner.setToolTipText(getString(KEY_LEVEL_TOOLTIP));
      JButton applyAllButton = new JButton(getString(KEY_SET_FOR_ALL));
      applyAllButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            applyLevelToAll();
         }
      });
      applyAllButton.setToolTipText(getString(KEY_SET_FOR_ALL_TOOLTIP));
      levelPanel.add(applyAllButton);
      ret.add(levelPanel, c);
      
      c.gridx += 1;
      c.weightx = 1.0;
      JPanel stringPanel = new JPanel(new GridBagLayout());
      GridBagConstraints sc = new GridBagConstraints();
      sc.fill = GridBagConstraints.HORIZONTAL;
      sc.gridx = 1;
      stringPanel.add(new JLabel(getString(KEY_NAME)), sc);
      textField = new JTextField();
      sc.gridx += 1;
      sc.weightx = 1.0;
      sc.insets = new Insets(0, 5, 0, 5);
      stringPanel.add(textField, sc);
      stringPanel.setToolTipText(getString(KEY_NAME_TOOLTIP));
      textField.setToolTipText(getString(KEY_NAME_TOOLTIP));
      ret.add(stringPanel, c);
      
      c.gridx += 1;
      c.weightx = 0.0;
      megaFilter = new JCheckBox(getString(KEY_MEGA_FILTER));
      megaFilter.setToolTipText(getString(KEY_MEGA_FILTER_TOOLTIP));
      ret.add(megaFilter, c);
      
      c.gridx += 1;
      c.weightx = 0.0;
      effectFilter = new EffectChooser(false, EffectChooser.DefaultEntry.NO_FILTER);
      effectFilter.setToolTipText(getString(KEY_EFFECT_FILTER_TOOLTIP));
      ret.add(effectFilter, c);
      
      getMinUpperPanel = new Supplier<Dimension>() {
         
         @Override
         public Dimension get() {
            Dimension ret = new Dimension(10 + 50, 0);
            for (Component c : new Component[] { typePanel, levelPanel, stringPanel, megaFilter, effectFilter }) {
               Dimension temp = c.getPreferredSize();
               int width = temp.width + ret.width;
               int height = Math.max(temp.height, ret.height);
               ret.setSize(width, height);
            }
            return ret;
         }
      };
      
      return ret;
   }
   
   @SuppressWarnings("serial")
   private Component makeCenterPanel() {
      rosterEntryPanel = new JPanel(new WrapLayout()) {
         // Fix to make it play nice with the scroll bar.
         @Override
         public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width = (int) (d.getWidth() - 20);
            return d;
         }
      };
      final JScrollPane ret = new JScrollPane(rosterEntryPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
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
      c.insets = new Insets(0, 10, 0, 10);
      c.weightx = 1.0;
      c.weighty = 0.0;
      c.gridx = 1;
      c.gridy = 1;
      c.gridwidth = 1;
      c.gridheight = 1;
      
      c.anchor = GridBagConstraints.LINE_START;
      c.weightx = 0.0;
      c.gridx++;
      selectedDisplayLabel = new JLabel(getString(KEY_NONE_SELECTED));
      selectedDisplayLabel.setToolTipText(getString(KEY_SELECTED_TOOLTIP));;
      ret.add(selectedDisplayLabel, c);
      c.insets = new Insets(0, 0, 0, 0);
      
      c.anchor = GridBagConstraints.LINE_END;
      c.weightx = 1.0;
      c.gridx++;
      teamFilter = new JCheckBox(getString(KEY_TEAM));
      JPanel teamFilterPanel = new JPanel(new BorderLayout());
      teamFilterPanel.add(teamFilter, BorderLayout.WEST);
      teamFilter.setToolTipText(getString(KEY_TEAM_TOOLTIP));
      ret.add(teamFilterPanel, c);
      
      c.anchor = GridBagConstraints.LINE_END;
      c.weightx = 0.0;
      c.gridx++;
      activeEffect = new EffectChooser(false, EffectChooser.DefaultEntry.SPECIES);
      JPanel activeEffectPanel = new JPanel(new BorderLayout());
      activeEffectPanel.add(activeEffect, BorderLayout.WEST);
      activeEffect.setToolTipText(getString(KEY_ACTIVE_EFFECT));
      ret.add(activeEffectPanel, c);
      
      c.anchor = GridBagConstraints.LINE_END;
      c.weightx = 0.0;
      c.gridx++;
      JPanel skillPanel = new JPanel(new BorderLayout());
      ImageIcon skillBoosterIcon = getUser().getImageManager().getImageFor(KEY_SKILL_BOOSTER);
      JLabel skillBoosterLabel = new JLabel(skillBoosterIcon);
      skillPanel.add(skillBoosterLabel, BorderLayout.EAST);
      skillLevels = new JComboBox<Integer>();
      skillLevels.setEnabled(false);
      skillLevels.addItem(1);
      skillPanel.add(skillLevels, BorderLayout.WEST);
      skillPanel.setToolTipText(getString(KEY_SKILL_BOOSTER_TOOLTIP));
      skillLevels.setToolTipText(getString(KEY_SKILL_BOOSTER_TOOLTIP));
      ret.add(skillPanel, c);
      
      c.anchor = GridBagConstraints.LINE_END;
      c.weightx = 0.0;
      c.gridx++;
      JPanel speedupPanel = new JPanel(new BorderLayout());
      ImageIcon candyIcon = getUser().getImageManager().getImageFor(KEY_CANDY_ICON);
      JLabel candyLabel = new JLabel(candyIcon);
      speedupPanel.add(candyLabel, BorderLayout.EAST);
      speedups = new JComboBox<Integer>();
      speedups.setEnabled(false);
      speedups.addItem(0);
      speedupPanel.add(speedups, BorderLayout.WEST);
      speedupPanel.setToolTipText(getString(KEY_CANDY_TOOLTIP));
      speedups.setToolTipText(getString(KEY_CANDY_TOOLTIP));
      ret.add(speedupPanel, c);
      
      c.anchor = GridBagConstraints.LINE_END;
      c.weightx = 0.0;
      c.gridx++;
      JButton okButton = new JButton(getString(KEY_OK));
      okButton.setToolTipText(getString(KEY_OK_TOOLTIP));
      ret.add(okButton, c);
      setDefaultButton(okButton);
      
      c.anchor = GridBagConstraints.CENTER;
      c.weightx = 0.0;
      c.gridx++;
      JButton applyButton = new JButton(getString(KEY_APPLY));
      applyButton.setToolTipText(getString(KEY_APPLY_TOOLTIP));
      ret.add(applyButton, c);
      
      c.anchor = GridBagConstraints.LINE_START;
      c.weightx = 0.0;
      c.gridx++;
      JButton cancelButton = new JButton(new DisposeAction(getString(KEY_CANCEL), this));
      cancelButton.setToolTipText(getString(KEY_CANCEL_TOOLTIP));
      ret.add(cancelButton, c);
      
      okButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            onOK();
         }
      });
      applyButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            onApply();
         }
      });
      addSpeedupsListener();
      return ret;
   }
   
   private void onOK() {
      onApply();
      dispose();
   }
   
   private void onApply() {
      getUser().loadFromRosterManager(myData);
   }
   
   @Override
   protected void onHide() {
      // Do nothing
   }
   
   @Override
   protected void onDispose() {
      getUser().deleteObserver(this);
      onHide();
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#updateGUIFrom(java.lang.Object)
    */
   @Override
   protected void updateGUIFrom(EditRosterServiceUser user) {
      myData = new RosterManager(user.getRosterManager());
      updateTeamSpeciesFromUser(user);
      updateRosterEntryPanel();
   }
   
   @Override
   public void update() {
      updateTeamSpeciesFromUser(getUser());
      updateRosterEntryPanel();
   }
   
   private void updateTeamSpeciesFromUser(EditRosterServiceUser user) {
      teamSpecies.clear();
      teamSpecies.addAll(user.getCurrentSpecies());
   }
   
   protected void updateRosterEntryPanel() {
      SpeciesManager speciesManager = getUser().getSpeciesManager();
      List<Predicate<Species>> filters = getCurrentFilters(false);
      Collection<Species> speciesValues = speciesManager.getSpeciesByFilters(filters);
      rosterEntryPanel.removeAll();
      Species newSpecies = null;
      JPanel newComponent = null;
      for (Species s : speciesValues) {
         JPanel component = createRosterComponent(s);
         if (s.equals(selectedSpecies)) {
            newSpecies = s;
            newComponent = component;
            setBorderFor(component, true);
         } else {
            setBorderFor(component, false);
         }
         rosterEntryPanel.add(component);
      }
      setSelected(newSpecies, newComponent);
      d.repaint();
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
            selectedDisplayLabel.repaint();
         }
         
         @Override
         protected void onEnter() {
            // Do nothing
         }
      };
      SpeciesPaint sp = new SpeciesPaint(s, false, getMegaFilter());
      ImageIcon icon = getUser().getImageManager().getImageFor(sp);
      JLabel iconLabel = new JLabel(icon);
      iconLabel.addMouseListener(ma);
      ret.add(iconLabel, c);
      c.gridy += 1;
      JLabel jLabel = new JLabel(s.getLocalizedName(getMegaFilter()));
      jLabel.setHorizontalTextPosition(SwingConstants.CENTER);
      jLabel.setHorizontalAlignment(SwingConstants.CENTER);
      jLabel.addMouseListener(ma);
      ret.add(jLabel, c);
      JComboBox<Integer> level = new JComboBox<Integer>();
      for (int i = 0; i <= Species.MAX_LEVEL; i++) {
         level.addItem(i);
      }
      Integer thisLevel = getLevelFor(s);
      level.setSelectedItem(thisLevel);
      level.setToolTipText(getString(KEY_POKEMON_LEVEL_TOOLTIP));
      c.gridy += 1; // put the level selector below the icon.
      ret.add(level, c);
      level.addItemListener(new ItemListener() {
         @Override
         public void itemStateChanged(ItemEvent e) {
            int index = level.getSelectedIndex();
            myData.setLevelForSpecies(s, index);
            rebuildSelectedLabel();
         }
      });
      return ret;
   }
   
   /**
    * @param s
    * @return
    */
   private Integer getLevelFor(Species species) {
      return myData.getLevelForSpecies(species);
   }
   
   private void setSelected(Species s, JPanel newComponent) {
      selectedSpecies = s;
      if (selectedComponent != null) {
         setBorderFor(selectedComponent, false);
      }
      selectedComponent = newComponent;
      setBorderFor(selectedComponent, true);
      rebuildSelectedLabel();
   }
   
   private void rebuildSelectedLabel() {
      String textToUse = getString(KEY_NONE_SELECTED);
      if (selectedSpecies != null) {
         String name = selectedSpecies.getLocalizedName();
         Integer thisLevel = myData.getLevelForSpecies(selectedSpecies);
         int attack = selectedSpecies.getAttack(thisLevel);
         PkmType type = megaFilter.isSelected() ? selectedSpecies.getMegaType() : selectedSpecies.getType();
         String typeNice = WordUtils.capitalizeFully(type.toString());
         Effect effect = selectedSpecies.getEffect(myData);
         String effectNice = EffectChooser.convertToBox(effect.toString());
         textToUse = getString(KEY_SELECTED, name, attack, typeNice, effectNice);
      }
      selectedDisplayLabel.setText(textToUse);
      final boolean isMega = selectedSpecies != null && selectedSpecies.getMegaName() != null;
      removeSpeedupsListener();
      speedups.setEnabled(isMega);
      speedups.removeAllItems();
      if (isMega) {
         int megaSpeedupCap = getUser().getEffectManager().getMegaSpeedupCap(selectedSpecies);
         for (int i = 0; i <= megaSpeedupCap; i++) {
            speedups.addItem(i);
         }
         int megaSpeedups = Math.min(myData.getMegaSpeedupsFor(selectedSpecies), megaSpeedupCap);
         speedups.setSelectedItem(megaSpeedups);
      }
      addSpeedupsListener();
      removeSkillLevelListener();
      skillLevels.setEnabled(selectedSpecies != null);
      skillLevels.removeAllItems();
      if (selectedSpecies != null) {
         for (int i = 1; i <= 5; i++) {
            skillLevels.addItem(i);
         }
         int skillLevel = Math.min(Math.max(myData.getSkillLevelForSpecies(selectedSpecies), 1), 5);
         skillLevels.setSelectedItem(skillLevel);
      }
      addSkillLevelListener();
      removeActiveEffectListener();
      if (selectedSpecies != null && selectedSpecies.getEffects().size() > 1) {
         activeEffect.setEnabled(true);
         activeEffect.setSpecies(selectedSpecies);
         activeEffect.setSelectedEffect(selectedSpecies.getEffect(myData));
      } else {
         activeEffect.removeAllItems();
         activeEffect.setEnabled(false);
      }
      addActiveEffectListener();
   }
   
   /**
    * 
    */
   private void addSpeedupsListener() {
      if (speedupsListener == null) {
         speedupsListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
               Integer num = speedups.getItemAt(speedups.getSelectedIndex());
               if (num == null) {
                  num = 0;
               }
               myData.setMegaSpeedupsFor(selectedSpecies, num);
               rebuildSelectedLabel();
            }
         };
      }
      speedups.addItemListener(speedupsListener);
   }
   
   private void removeSpeedupsListener() {
      speedups.removeItemListener(speedupsListener);
   }
   
   private void addSkillLevelListener() {
      if (skillLevelsListener == null) {
         skillLevelsListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
               Integer num = skillLevels.getItemAt(skillLevels.getSelectedIndex());
               if (num == null) {
                  num = 1;
               }
               myData.setSkillLevelForSpecies(selectedSpecies, num);
               rebuildSelectedLabel();
            }
         };
      }
      skillLevels.addItemListener(skillLevelsListener);
   }
   
   private void removeSkillLevelListener() {
      skillLevels.removeItemListener(skillLevelsListener);
   }
   
   private void addActiveEffectListener() {
      if (activeEffectListener == null) {
         activeEffectListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
               Effect selectedEffect = activeEffect.getSelectedEffect();
               myData.setActiveEffect(selectedSpecies, selectedEffect);
               rebuildSelectedLabel();
            }
         };
      }
      activeEffect.addItemListener(activeEffectListener);
   }
   
   private void removeActiveEffectListener() {
      activeEffect.removeItemListener(activeEffectListener);
   }
   
   private void setBorderFor(JPanel c, boolean doBorder) {
      if (c != null) {
         ConfigManager manager = getUser().getPreferencesManager();
         int borderThick = manager.getIntegerValue(KEY_ROSTER_CELL_BORDER_THICK, DEFAULT_BORDER_WIDTH);
         borderThick = getUser().scaleBorderThickness(borderThick);
         int outlineThick = manager.getIntegerValue(KEY_ROSTER_CELL_OUTLINE_THICK, DEFAULT_BORDER_OUTLINE);
         outlineThick = getUser().scaleBorderThickness(outlineThick);
         int marginThick = manager.getIntegerValue(KEY_ROSTER_CELL_MARGIN_THICK, DEFAULT_BORDER_MARGIN);
         marginThick = getUser().scaleBorderThickness(marginThick);
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
   
   private void applyLevelToAll() {
      Collection<Species> species = getFilteredSpecies(true);
      if (species.isEmpty()) {
         species = getFilteredSpecies(false);
      }
      if (species.isEmpty()) {
         species = getUser().getSpeciesManager().getSpeciesByFilters(getBasicFilters());
      }
      Integer levelToSet = getLevel();
      if (levelToSet == null) {
         levelToSet = 0;
      }
      for (Species s : species) {
         myData.setLevelForSpecies(s, levelToSet);
      }
      updateRosterEntryPanel();
   }
   
   private Effect getEffect() {
      return effectFilter.getSelectedEffect();
   }
   
   private boolean getMegaFilter() {
      return megaFilter.isSelected();
   }
   
   private boolean getTeamFilter() {
      return teamFilter.isSelected();
   }
   
   private Stage getCurrentStage() {
      return getUser().getCurrentStage();
   }
   
   private Function<PkmType, Boolean> getTypeFilter(PkmType stageType) {
      return typeChooser.getCurrentFilter(stageType);
   }
   
   private String getContainsString() {
      return textField.getText();
   }
   
   private Integer getLevel() {
      return (Integer) levelSpinner.getValue();
   }
   
   private Collection<Species> getFilteredSpecies(boolean ignoreLevel) {
      SpeciesManager manager = getUser().getSpeciesManager();
      return manager.getSpeciesByFilters(getCurrentFilters(ignoreLevel));
   }
   
   /**
    * @return
    */
   private List<Predicate<Species>> getCurrentFilters(boolean ignoreLevel) {
      List<Predicate<Species>> filters = getBasicFilters();
      Function<PkmType, Boolean> typeFilter = getTypeFilter(getCurrentStage().getType());
      filters.add(species -> typeFilter.apply(megaFilter.isSelected() ? species.getMegaType() : species.getType()));
      if (!ignoreLevel) {
         Integer curLevelFilter = getLevel();
         int minLevel = curLevelFilter != null ? curLevelFilter : 0;
         filters.add(species -> myData.getLevelForSpecies(species.getName()) >= minLevel);
      }
      String str = getContainsString().toUpperCase();
      if (!str.isEmpty()) {
         filters.add(species -> species.getLocalizedName(megaFilter.isSelected()).toUpperCase().contains(str));
      }
      Effect effect = getEffect();
      if (effect != null) {
         filters.add(species -> species.getEffect(myData).equals(effect));
      }
      if (getMegaFilter()) {
         filters.add(species -> species.getMegaName() != null && !species.getMegaName().isEmpty());
      }
      if (getTeamFilter()) {
         filters.add(species -> teamSpecies.contains(species));
      }
      return filters;
   }
   
   /**
    * @return
    */
   private List<Predicate<Species>> getBasicFilters() {
      List<Predicate<Species>> filters = new ArrayList<Predicate<Species>>();
      filters.add(species -> species.getEffect(myData).canLevel());
      return filters;
   }
   
   /*
    * (non-Javadoc)
    * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
    */
   @Override
   public void update(Observable o, Object arg) {
      update();
   }
}
