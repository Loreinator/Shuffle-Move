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

package shuffle.fwk.service.teams;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
import shuffle.fwk.config.manager.StageManager;
import shuffle.fwk.config.manager.TeamManager;
import shuffle.fwk.data.Effect;
import shuffle.fwk.data.PkmType;
import shuffle.fwk.data.Species;
import shuffle.fwk.data.SpeciesPaint;
import shuffle.fwk.data.Stage;
import shuffle.fwk.data.Team;
import shuffle.fwk.data.TeamImpl;
import shuffle.fwk.gui.EffectChooser;
import shuffle.fwk.gui.Indicator;
import shuffle.fwk.gui.MultiListener;
import shuffle.fwk.gui.PressOrClickMouseAdapter;
import shuffle.fwk.gui.StageChooser;
import shuffle.fwk.gui.TypeChooser;
import shuffle.fwk.gui.WrapLayout;
import shuffle.fwk.gui.user.MultiListenerUser;
import shuffle.fwk.gui.user.StageIndicatorUser;
import shuffle.fwk.i18n.I18nUser;
import shuffle.fwk.service.BaseService;
import shuffle.fwk.service.DisposeAction;
import shuffle.fwk.service.roster.EditRosterService;

/**
 * @author Andrew Meyers
 *
 */
public class EditTeamService extends BaseService<EditTeamServiceUser> implements StageIndicatorUser, I18nUser,
      MultiListenerUser {
   private static final Logger LOG = Logger.getLogger(EditTeamService.class.getName());
   
   // Config keys
   private static final String KEY_ROSTER_CELL_OUTLINE_THICK = EditRosterService.KEY_ROSTER_CELL_OUTLINE_THICK;
   private static final String KEY_ROSTER_CELL_BORDER_THICK = EditRosterService.KEY_ROSTER_CELL_BORDER_THICK;
   private static final String KEY_ROSTER_CELL_MARGIN_THICK = EditRosterService.KEY_ROSTER_CELL_MARGIN_THICK;
   private static final String KEY_EDIT_TEAM_WIDTH = "EDIT_TEAM_WIDTH";
   private static final String KEY_EDIT_TEAM_HEIGHT = "EDIT_TEAM_HEIGHT";
   
   // i18n keys
   private static final String KEY_NO_BINDINGS = "error.nobindings";
   private static final String KEY_NONE_SELECTED = "text.noneselected";
   private static final String KEY_SELECTED = "text.selected";
   private static final String KEY_OK = "button.ok";
   private static final String KEY_APPLY = "button.apply";
   private static final String KEY_CANCEL = "button.cancel";
   private static final String KEY_NONE = "text.none";
   private static final String KEY_ACTIVE = "text.active";
   private static final String KEY_MAKE_DEFAULT = "button.makedefault";
   private static final String KEY_MEGA_LABEL = "text.megalabel";
   private static final String KEY_MEGA_FILTER = "text.megafilter";
   private static final String KEY_CLEAR_TEAM = "button.clearteam";
   private static final String KEY_WOOD = "text.wood";
   private static final String KEY_METAL = "text.metal";
   private static final String KEY_COIN = "text.coin";
   private static final String KEY_REMOVE = "button.remove";
   private static final String KEY_ADD = "button.add";
   private static final String KEY_TITLE = "text.title";
   private static final String KEY_LEVEL = "text.level";
   private static final String KEY_NAME = "text.name";
   private static final String KEY_TYPE = "text.type";
   
   // Defaults
   private static final int DEFAULT_BORDER_WIDTH = EditRosterService.DEFAULT_BORDER_WIDTH;
   private static final int DEFAULT_BORDER_OUTLINE = EditRosterService.DEFAULT_BORDER_OUTLINE;
   private static final int DEFAULT_BORDER_MARGIN = EditRosterService.DEFAULT_BORDER_MARGIN;
   
   // components and data
   private TypeChooser typeChooser = null;
   private JLabel selectedDisplayLabel = null;
   private JPanel rosterPanel = null;
   private JPanel teamPanel = null;
   private JSpinner levelSpinner = null;
   private JTextField textField = null;
   private JCheckBox megaFilter = null;
   private EffectChooser effectFilter = null;
   private JDialog d = null;
   private TeamManager myData = null;
   private Map<String, ItemListener> nameToItemListenerMap = new HashMap<String, ItemListener>();
   private Map<String, JComboBox<Character>> nameToKeybindComboboxMap = new HashMap<String, JComboBox<Character>>();
   private Stage curStage = StageManager.DEFAULT_STAGE;
   private Team prevTeam = null;
   private JComboBox<String> megaChooser;
   private JCheckBox megaActive;
   private boolean wasMegaActive = false;
   private JComboBox<Integer> megaProgressChooser;
   private int megaProgress = 0;
   private int megaThreshold = 0;
   private JCheckBox woodCheckBox;
   private JCheckBox metalCheckBox;
   private JCheckBox coinCheckBox;
   private StageChooser stageChooser;
   private JScrollPane rosterScrollPane;
   
   private JPanel selectedComponent = null;
   private Species selectedSpecies = null;
   
   private ItemListener optionListener = null;
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#getUserClass()
    */
   @Override
   protected Class<EditTeamServiceUser> getUserClass() {
      return EditTeamServiceUser.class;
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#setupGUI()
    */
   @Override
   public void onSetupGUI() {
      d = new JDialog(getOwner(), getString(KEY_TITLE));
      d.setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 1.0;
      c.weighty = 0.0;
      c.gridx = 1;
      c.gridy = 1;
      c.gridwidth = 2;
      c.gridheight = 1;
      d.add(makeUpperPanel(), c);
      
      c.gridy += 1;
      c.fill = GridBagConstraints.BOTH;
      c.weighty = 1.0;
      c.gridwidth = 1;
      d.add(makeRosterPanel(), c);
      
      c.gridx += 1;
      c.weightx = 0.0;
      d.add(makeTeamPanel(), c);
      c.weightx = 1.0;
      
      c.gridx = 1;
      c.gridy += 1;
      c.gridwidth = 2;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weighty = 0.0;
      d.add(makeBottomPanel(), c);
      
      ConfigManager preferencesManager = getUser().getPreferencesManager();
      int defaultWidth = preferencesManager.getIntegerValue(KEY_POPUP_WIDTH, DEFAULT_POPUP_WIDTH);
      int defaultHeight = preferencesManager.getIntegerValue(KEY_POPUP_HEIGHT, DEFAULT_POPUP_HEIGHT);
      int width = preferencesManager.getIntegerValue(KEY_EDIT_TEAM_WIDTH, defaultWidth);
      int height = preferencesManager.getIntegerValue(KEY_EDIT_TEAM_HEIGHT, defaultHeight);
      d.repaint();
      d.pack();
      d.setMinimumSize(new Dimension(DEFAULT_POPUP_WIDTH, DEFAULT_POPUP_HEIGHT));
      d.setSize(new Dimension(width, height));
      d.setLocationRelativeTo(null);
      d.setResizable(true);
      addActionListeners();
      
      setDialog(d);
   }
   
   @Override
   protected void onLaunch() {
      d.addComponentListener(new ComponentAdapter() {
         @Override
         public void componentResized(ComponentEvent ev) {
            ConfigManager preferencesManager = getUser().getPreferencesManager();
            Dimension dim = d.getSize();
            preferencesManager.setEntry(EntryType.INTEGER, KEY_EDIT_TEAM_WIDTH, dim.width);
            preferencesManager.setEntry(EntryType.INTEGER, KEY_EDIT_TEAM_HEIGHT, dim.height);
         }
      });
   }
   
   private void addActionListeners() {
      MultiListener listener = new MultiListener(this);
      typeChooser.addItemListener(listener);
      textField.getDocument().addDocumentListener(listener);
      levelSpinner.getModel().addChangeListener(listener);
      megaFilter.addItemListener(listener);
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
      ret.add(typePanel, c);
      
      c.gridx += 1;
      c.weightx = 0.0;
      JPanel levelPanel = new JPanel();
      levelPanel.add(new JLabel(getString(KEY_LEVEL)));
      SpinnerNumberModel snm = new SpinnerNumberModel(0, 0, 10, 1);
      levelSpinner = new JSpinner(snm);
      levelPanel.add(levelSpinner);
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
      ret.add(stringPanel, c);
      
      c.gridx += 1;
      c.weightx = 0.0;
      megaFilter = new JCheckBox(getString(KEY_MEGA_FILTER));
      ret.add(megaFilter, c);
      
      c.gridx += 1;
      c.weightx = 0.0;
      effectFilter = new EffectChooser(false, true);
      ret.add(effectFilter, c);
      
      c.gridx += 1;
      c.weightx = 0.0;
      @SuppressWarnings("serial")
      JButton copyToLauncher = new JButton(new AbstractAction(getString(KEY_MAKE_DEFAULT)) {
         @Override
         public void actionPerformed(ActionEvent e) {
            makeTeamDefault();
         }
      });
      ret.add(copyToLauncher, c);
      
      return ret;
   }
   
   /**
	 * 
	 */
   protected void makeTeamDefault() {
      Stage curStage = getCurrentStage();
      Team curTeam = myData.getTeamForStage(curStage);
      PkmType t = curStage.getType();
      Stage fallbackStage = new Stage(t);
      myData.setTeamForStage(new TeamImpl(curTeam), fallbackStage);
      updateTeamPanel();
   }
   
   @SuppressWarnings("serial")
   private Component makeRosterPanel() {
      rosterPanel = new JPanel(new WrapLayout()) {
         // Fix to make it play nice with the scroll bar.
         @Override
         public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width = (int) (d.getWidth() - 20);
            return d;
         }
      };
      rosterScrollPane = new JScrollPane(rosterPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      rosterScrollPane.addComponentListener(new ComponentAdapter() {
         @Override
         public void componentResized(ComponentEvent e) {
            rosterScrollPane.revalidate();
         }
      });
      rosterScrollPane.getVerticalScrollBar().setUnitIncrement(27);
      return rosterScrollPane;
   }
   
   @SuppressWarnings("serial")
   private Component makeTeamPanel() {
      
      JPanel firstOptionRow = new JPanel(new GridBagLayout());
      GridBagConstraints rowc = new GridBagConstraints();
      rowc.fill = GridBagConstraints.HORIZONTAL;
      rowc.weightx = 0.0;
      rowc.weighty = 0.0;
      
      rowc.weightx = 1.0;
      rowc.gridx = 1;
      stageChooser = new StageChooser(this);
      firstOptionRow.add(stageChooser, rowc);
      rowc.weightx = 0.0;
      
      JPanel secondOptionRow = new JPanel(new GridBagLayout());
      
      rowc.gridx = 1;
      JLabel megaLabel = new JLabel(getString(KEY_MEGA_LABEL));
      secondOptionRow.add(megaLabel, rowc);
      
      rowc.gridx = 2;
      megaChooser = new JComboBox<String>();
      secondOptionRow.add(megaChooser, rowc);
      
      rowc.gridx = 3;
      JPanel progressPanel = new JPanel(new BorderLayout());
      megaActive = new JCheckBox(getString(KEY_ACTIVE));
      megaActive.setSelected(false);
      progressPanel.add(megaActive, BorderLayout.WEST);
      megaProgressChooser = new JComboBox<Integer>();
      progressPanel.add(megaProgressChooser, BorderLayout.EAST);
      secondOptionRow.add(progressPanel, rowc);
      
      JPanel thirdOptionRow = new JPanel(new GridBagLayout());
      
      rowc.gridx = 1;
      JButton clearTeamButton = new JButton(getString(KEY_CLEAR_TEAM));
      clearTeamButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            clearTeam();
         }
      });
      thirdOptionRow.add(clearTeamButton, rowc);
      
      rowc.gridx = 2;
      woodCheckBox = new JCheckBox(getString(KEY_WOOD));
      thirdOptionRow.add(woodCheckBox, rowc);
      
      rowc.gridx = 3;
      metalCheckBox = new JCheckBox(getString(KEY_METAL));
      thirdOptionRow.add(metalCheckBox, rowc);
      
      rowc.gridx = 4;
      coinCheckBox = new JCheckBox(getString(KEY_COIN));
      thirdOptionRow.add(coinCheckBox, rowc);
      
      JPanel topPart = new JPanel(new GridBagLayout());
      GridBagConstraints topC = new GridBagConstraints();
      topC.fill = GridBagConstraints.HORIZONTAL;
      topC.weightx = 0.0;
      topC.weighty = 0.0;
      topC.gridx = 1;
      topC.gridy = 1;
      topC.gridwidth = 1;
      topC.gridheight = 1;
      topC.anchor = GridBagConstraints.CENTER;
      
      topC.gridy = 1;
      topPart.add(firstOptionRow, topC);
      topC.gridy = 2;
      topPart.add(secondOptionRow, topC);
      topC.gridy = 3;
      topPart.add(thirdOptionRow, topC);
      
      addOptionListeners();
      
      teamPanel = new JPanel(new WrapLayout()) {
         // Fix to make it play nice with the scroll bar.
         @Override
         public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width = (int) (d.getWidth() - 20);
            return d;
         }
      };
      final JScrollPane scrollPane = new JScrollPane(teamPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) {
         @Override
         public Dimension getMinimumSize() {
            Dimension d = super.getMinimumSize();
            d.width = topPart.getMinimumSize().width;
            d.height = rosterScrollPane.getPreferredSize().height - topPart.getPreferredSize().height;
            return d;
         }
         
         @Override
         public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width = topPart.getMinimumSize().width;
            d.height = rosterScrollPane.getPreferredSize().height - topPart.getPreferredSize().height;
            return d;
         }
      };
      scrollPane.addComponentListener(new ComponentAdapter() {
         @Override
         public void componentResized(ComponentEvent e) {
            scrollPane.revalidate();
         }
      });
      scrollPane.getVerticalScrollBar().setUnitIncrement(27);
      
      JPanel ret = new JPanel(new GridBagLayout());
      GridBagConstraints rc = new GridBagConstraints();
      rc.fill = GridBagConstraints.VERTICAL;
      rc.weightx = 0.0;
      rc.weighty = 0.0;
      rc.gridx = 1;
      rc.gridy = 1;
      rc.insets = new Insets(5, 5, 5, 5);
      ret.add(topPart, rc);
      rc.gridy += 1;
      rc.weightx = 0.0;
      rc.weighty = 1.0;
      rc.insets = new Insets(0, 0, 0, 0);
      ret.add(scrollPane, rc);
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
      
      c.anchor = GridBagConstraints.LINE_START;
      c.weightx = 0.0;
      c.gridx += 1;
      c.insets = new Insets(0, 10, 0, 10);
      selectedDisplayLabel = new JLabel(getString(KEY_NONE_SELECTED));
      ret.add(selectedDisplayLabel, c);
      
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
            onOK();
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
   
   private void onOK() {
      onApply();
      dispose();
   }
   
   private void onApply() {
      getUser().loadFromTeamManager(myData);
      getUser().setCurrentStage(getCurrentStage());
      getUser().setMegaProgress(megaProgress);
   }
   
   @Override
   protected void onHide() {
      // Do Nothing
   }
   
   @Override
   protected void onDispose() {
      onHide();
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.service.BaseService#updateGUIFrom(java.lang.Object)
    */
   @Override
   protected void updateGUIFrom(EditTeamServiceUser user) {
      myData = new TeamManager(user.getTeamManager());
      curStage = user.getCurrentStage();
      megaProgress = user.getMegaProgress();
      megaThreshold = getCurrentTeam().getMegaThreshold(user.getSpeciesManager(), getUser().getRosterManager());
      updateRosterPanel();
      updateTeamPanel();
   }
   
   @Override
   public void update() {
      updateRosterPanel();
   }
   
   private void updateRosterPanel() {
      rosterPanel.removeAll();
      
      Species newSpecies = null;
      JPanel newComponent = null;
      SpeciesManager speciesManager = getUser().getSpeciesManager();
      List<Predicate<Species>> filters = getCurrentFilters(false);
      Collection<Species> speciesValues = speciesManager.getSpeciesByFilters(filters);
      ConfigManager manager = getUser().getPreferencesManager();
      int borderThick = manager.getIntegerValue(KEY_ROSTER_CELL_BORDER_THICK, DEFAULT_BORDER_WIDTH);
      int outlineThick = manager.getIntegerValue(KEY_ROSTER_CELL_OUTLINE_THICK, DEFAULT_BORDER_OUTLINE);
      int marginThick = manager.getIntegerValue(KEY_ROSTER_CELL_MARGIN_THICK, DEFAULT_BORDER_MARGIN);
      for (Species s : speciesValues) {
         JPanel component = createRosterComponent(s);
         if (s.equals(selectedSpecies)) {
            newSpecies = s;
            newComponent = component;
            setBorderFor(component, true, true, borderThick, marginThick, outlineThick);
         } else {
            setBorderFor(component, false, true, borderThick, marginThick, outlineThick);
         }
         rosterPanel.add(component);
      }
      setSelected(newSpecies, newComponent);
      rosterPanel.revalidate();
      d.repaint();
   }
   
   private JPanel createRosterComponent(Species s) {
      JPanel ret = new JPanel(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.HORIZONTAL;
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
      
      ImageIcon icon = getUser().getImageManager().getImageFor(s);
      JLabel iconLabel = new JLabel(icon);
      iconLabel.addMouseListener(ma);
      ret.add(iconLabel, c);
      c.gridy += 1;
      String text = s.getName();
      JLabel jLabel = new JLabel(text);
      jLabel.setHorizontalTextPosition(SwingConstants.CENTER);
      jLabel.setHorizontalAlignment(SwingConstants.CENTER);
      jLabel.addMouseListener(ma);
      ret.add(jLabel, c);
      
      JButton addToTeam = new JButton(getString(KEY_ADD));
      addToTeam.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            addSpeciesToTeam(s);
            updateTeamPanel();
         }
      });
      c.gridy += 1;
      ret.add(addToTeam, c);
      
      return ret;
   }
   
   private void setSelected(Species s, JPanel newComponent) {
      selectedSpecies = s;
      ConfigManager manager = getUser().getPreferencesManager();
      int borderThick = manager.getIntegerValue(KEY_ROSTER_CELL_BORDER_THICK, DEFAULT_BORDER_WIDTH);
      int outlineThick = manager.getIntegerValue(KEY_ROSTER_CELL_OUTLINE_THICK, DEFAULT_BORDER_OUTLINE);
      int marginThick = manager.getIntegerValue(KEY_ROSTER_CELL_MARGIN_THICK, DEFAULT_BORDER_MARGIN);
      if (selectedComponent != null) {
         setBorderFor(selectedComponent, false, true, borderThick, marginThick, outlineThick);
      }
      selectedComponent = newComponent;
      setBorderFor(selectedComponent, true, true, borderThick, marginThick, outlineThick);
      rebuildSelectedLabel();
   }
   
   private void rebuildSelectedLabel() {
      String textToUse = getString(KEY_NONE_SELECTED);
      if (selectedSpecies != null) {
         String name = selectedSpecies.getName().replaceAll("_", " ");
         RosterManager rosterManager = getUser().getRosterManager();
         Integer thisLevel = rosterManager.getLevelForSpecies(selectedSpecies);
         int attack = selectedSpecies.getAttack(thisLevel);
         PkmType type = selectedSpecies.getType();
         String typeNice = WordUtils.capitalizeFully(type.toString());
         Effect effect = selectedSpecies.getEffect();
         String effectNice = WordUtils.capitalizeFully(effect.toString(), '_').replaceAll("_", " ");
         if (effectNice.endsWith(" P") && effectNice.length() > 2) {
            effectNice = effectNice.substring(0, effectNice.length() - 2) + "+";
         }
         textToUse = getString(KEY_SELECTED, name, attack, typeNice, effectNice);
      }
      selectedDisplayLabel.setText(textToUse);
   }
   
   private void setBorderFor(JComponent c, boolean isSelected, boolean haveSelect, int borderThick, int marginThick,
         int outlineThick) {
      if (c != null) {
         Border margin = new EmptyBorder(marginThick, marginThick, marginThick, marginThick);
         Border greyOutline = new LineBorder(Color.gray, outlineThick);
         Border innerChunk = BorderFactory.createCompoundBorder(greyOutline, margin);
         Border toSet;
         if (haveSelect) {
            Border main;
            if (isSelected) {
               main = new LineBorder(Color.BLACK, borderThick);
            } else {
               main = new EmptyBorder(borderThick, borderThick, borderThick, borderThick);
            }
            Border outerChunk = BorderFactory.createCompoundBorder(main, margin);
            Border finalBorder = BorderFactory.createCompoundBorder(outerChunk, innerChunk);
            toSet = finalBorder;
         } else {
            toSet = innerChunk;
         }
         c.setBorder(toSet);
      }
   }
   
   private void updateTeamPanel() {
      stageChooser.updateStage();
      Team curTeam = myData.getTeamForStage(getCurrentStage());
      if (prevTeam != null && !prevTeam.getNames().isEmpty() && (curTeam == null || curTeam.getNames().isEmpty())) {
         curTeam = new TeamImpl(prevTeam);
         myData.setTeamForStage(curTeam, getCurrentStage());
      }
      prevTeam = null;
      
      teamPanel.removeAll();
      nameToKeybindComboboxMap.clear();
      nameToItemListenerMap.clear();
      List<String> teamNames = curTeam.getNames();
      Set<String> names = new HashSet<String>(teamNames);
      SpeciesManager speciesManager = getUser().getSpeciesManager();
      for (String name : teamNames) {
         Species species = speciesManager.getSpeciesByName(name);
         if (species != null) {
            teamPanel.add(createTeamComponent(species));
         }
      }
      teamPanel.repaint();
      
      removeOptionListeners();
      
      // update the special species checkboxes
      boolean hasWood = curTeam.getNames().contains(Species.WOOD.getName());
      boolean hasMetal = curTeam.getNames().contains(Species.METAL.getName());
      boolean hasCoin = curTeam.getNames().contains(Species.COIN.getName());
      woodCheckBox.setSelected(hasWood);
      metalCheckBox.setSelected(hasMetal);
      coinCheckBox.setSelected(hasCoin);
      
      megaChooser.removeAllItems();
      megaChooser.addItem(getString(KEY_NONE));
      for (String name : names) {
         Species species = speciesManager.getSpeciesValue(name);
         if (species.getMegaName() != null) {
            megaChooser.addItem(name);
         }
      }
      String megaSlotName = curTeam.getMegaSlotName();
      Species megaSpecies = megaSlotName == null ? null : speciesManager.getSpeciesValue(megaSlotName);
      // Mega chooser (which species is the mega slot) settings...
      if (megaSpecies == null || megaSpecies.getMegaName() == null) {
         megaChooser.setSelectedItem(getString(KEY_NONE));
      } else {
         megaChooser.setSelectedItem(megaSlotName);
      }
      int newThreshold = curTeam.getMegaThreshold(speciesManager, getUser().getRosterManager());
      if (megaSpecies == null || megaSpecies.getMegaName() == null || newThreshold == Integer.MAX_VALUE) {
         // remove their states
         megaProgressChooser.removeAllItems();
         megaActive.setSelected(false);
         // disable both the progress and the activate button
         megaProgressChooser.setEnabled(false);
         megaActive.setEnabled(false);
         megaProgress = 0;
         megaThreshold = Integer.MAX_VALUE;
      } else {
         // If we were at the max, we remain at the max
         // Otherwise, we cap out at the lesser of our previous progress and the new threshold
         if (megaProgress == megaThreshold) {
            megaProgress = newThreshold;
         } else {
            megaProgress = Math.min(megaProgress, newThreshold);
         }
         // update threshold
         megaThreshold = newThreshold;
         // refresh the progress dropdown menu
         megaProgressChooser.removeAllItems();
         int threshold = megaThreshold == Integer.MAX_VALUE ? 0 : megaThreshold;
         for (int i = 0; i <= threshold; i++) {
            megaProgressChooser.addItem(i);
         }
         // update their states appropriately
         megaProgressChooser.setSelectedItem(megaProgress);
         megaActive.setSelected(megaThreshold == megaProgress);
         // maintain the look-behind
         wasMegaActive = megaThreshold == megaProgress;
         // ensure they are editable
         megaProgressChooser.setEnabled(true);
         megaActive.setEnabled(true);
      }
      addOptionListeners();
   }
   
   private Component createTeamComponent(Species s) {
      Team curTeam = myData.getTeamForStage(getCurrentStage());
      JPanel ret = new JPanel(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.gridx = 1;
      c.gridy = 1;
      c.gridwidth = 2;
      Indicator<SpeciesPaint> ind = new Indicator<SpeciesPaint>(this);
      boolean isMega = megaProgress >= megaThreshold && s.getName().equals(curTeam.getMegaSlotName());
      SpeciesPaint paint = new SpeciesPaint(s, false, isMega);
      ind.setVisualized(paint);
      ret.add(ind, c);
      c.gridy += 1;
      c.gridwidth = 1;
      JButton removeButton = new JButton(getString(KEY_REMOVE));
      removeButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent arg0) {
            removeSpeciesFromTeam(s.getName());
            updateTeamPanel();
         }
      });
      removeButton.setEnabled(s.getEffect().isPickable() && !s.getType().equals(PkmType.NONE));
      ret.add(removeButton, c);
      
      c.gridx += 1;
      JComboBox<Character> keybindsComboBox = new JComboBox<Character>();
      Character curBinding = curTeam.getBinding(s);
      LinkedHashSet<Character> allBindingsFor = new LinkedHashSet<Character>(Arrays.asList(curBinding));
      LinkedHashSet<Character> availableBindings = myData.getAllAvailableBindingsFor(s.getName(), curTeam);
      allBindingsFor.addAll(availableBindings);
      for (Character ch : allBindingsFor) {
         keybindsComboBox.addItem(ch);
      }
      keybindsComboBox.setSelectedItem(curBinding);
      final ItemListener bindingListener = new ItemListener() {
         @Override
         public void itemStateChanged(ItemEvent e) {
            JComboBox<?> source = (JComboBox<?>) e.getSource();
            int selectedIndex = source.getSelectedIndex();
            Character selected = (Character) source.getItemAt(selectedIndex);
            setBinding(s, selected);
            updateKeybindComboBoxes();
         }
      };
      nameToKeybindComboboxMap.put(s.getName(), keybindsComboBox);
      nameToItemListenerMap.put(s.getName(), bindingListener);
      keybindsComboBox.addItemListener(bindingListener);
      ret.add(keybindsComboBox, c);
      
      ConfigManager manager = getUser().getPreferencesManager();
      int borderThick = manager.getIntegerValue(KEY_ROSTER_CELL_BORDER_THICK, DEFAULT_BORDER_WIDTH);
      int outlineThick = manager.getIntegerValue(KEY_ROSTER_CELL_OUTLINE_THICK, DEFAULT_BORDER_OUTLINE);
      int marginThick = manager.getIntegerValue(KEY_ROSTER_CELL_MARGIN_THICK, DEFAULT_BORDER_MARGIN);
      setBorderFor(ret, false, false, borderThick, marginThick, outlineThick);
      return ret;
   }
   
   private void setBinding(Species species, Character newBinding) {
      TeamImpl curTeam = new TeamImpl(myData.getTeamForStage(getCurrentStage()));
      curTeam.setBinding(species, newBinding);
      Team newTeam = curTeam;
      myData.setEntry(EntryType.TEAM, getCurrentStage().getName(), newTeam);
   }
   
   private void updateKeybindComboBoxes() {
      Team curTeam = myData.getTeamForStage(getCurrentStage());
      for (String name : curTeam.getNames()) {
         ItemListener itemListener = nameToItemListenerMap.get(name);
         JComboBox<Character> box = nameToKeybindComboboxMap.get(name);
         box.removeItemListener(itemListener);
         Character prevSelected = box.getItemAt(box.getSelectedIndex());
         box.removeAllItems();
         Character curBinding = curTeam.getBinding(name);
         LinkedHashSet<Character> availableBindings = new LinkedHashSet<Character>(Arrays.asList(curBinding));
         availableBindings.addAll(myData.getAllAvailableBindingsFor(name, curTeam));
         for (Character ch : availableBindings) {
            box.addItem(ch);
         }
         box.setSelectedItem(prevSelected);
         if (box.getSelectedIndex() < 0) {
            LOG.warning(getString(KEY_NO_BINDINGS));
         }
         box.addItemListener(itemListener);
      }
   }
   
   private void removeOptionListeners() {
      if (optionListener != null) {
         megaChooser.removeItemListener(optionListener);
         megaProgressChooser.removeItemListener(optionListener);
         megaActive.removeItemListener(optionListener);
         woodCheckBox.removeItemListener(optionListener);
         metalCheckBox.removeItemListener(optionListener);
         coinCheckBox.removeItemListener(optionListener);
      }
   }
   
   private void addOptionListeners() {
      if (optionListener == null) {
         optionListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
               updateFromOptions();
               updateTeamPanel();
            }
         };
      }
      megaChooser.addItemListener(optionListener);
      megaProgressChooser.addItemListener(optionListener);
      megaActive.addItemListener(optionListener);
      woodCheckBox.addItemListener(optionListener);
      metalCheckBox.addItemListener(optionListener);
      coinCheckBox.addItemListener(optionListener);
   }
   
   private void updateFromOptions() {
      TeamImpl curTeam = new TeamImpl(myData.getTeamForStage(getCurrentStage()));
      curTeam.setMegaSlot(getMegaSlot());
      String woodName = Species.WOOD.getName();
      boolean hasWood = curTeam.getNames().contains(woodName);
      String metalName = Species.METAL.getName();
      boolean hasMetal = curTeam.getNames().contains(metalName);
      String coinName = Species.COIN.getName();
      boolean hasCoin = curTeam.getNames().contains(coinName);
      
      if (hasWood && !woodCheckBox.isSelected()) {
         curTeam.removeName(woodName);
      } else if (!hasWood && woodCheckBox.isSelected()) {
         curTeam.addName(woodName, getNextBindingFor(woodName, curTeam));
      }
      if (hasMetal && !metalCheckBox.isSelected()) {
         curTeam.removeName(metalName);
      } else if (!hasMetal && metalCheckBox.isSelected()) {
         curTeam.addName(metalName, getNextBindingFor(metalName, curTeam));
      }
      if (hasCoin && !coinCheckBox.isSelected()) {
         curTeam.removeName(coinName);
      } else if (!hasCoin && coinCheckBox.isSelected()) {
         curTeam.addName(coinName, getNextBindingFor(coinName, curTeam));
      }
      Integer selectedMegaProgress = megaProgressChooser.getItemAt(megaProgressChooser.getSelectedIndex());
      if (selectedMegaProgress != null) {
         if (selectedMegaProgress == megaProgress && wasMegaActive != megaActive.isSelected()) {
            megaProgress = megaActive.isSelected() ? megaThreshold : 0;
         } else {
            megaProgress = selectedMegaProgress;
         }
         wasMegaActive = megaActive.isSelected();
      }
      myData.setTeamForStage(curTeam, getCurrentStage());
   }
   
   private char getNextBindingFor(String name, Team team) {
      return myData.getAllAvailableBindingsFor(name, team).iterator().next();
   }
   
   private Team getCurrentTeam() {
      return new TeamImpl(myData.getTeamForStage(getCurrentStage()));
   }
   
   private void addSpeciesToTeam(Species species) {
      TeamImpl curTeam = new TeamImpl(getCurrentTeam());
      curTeam.addName(species.getName(), getNextBindingFor(species.getName(), curTeam));
      if (curTeam.getMegaSlotName() == null && species.getMegaName() != null) {
         curTeam.setMegaSlot(species.getName());
      }
      if (curTeam.getMegaSlotName() != null) {
         megaThreshold = curTeam.getMegaThreshold(getUser().getSpeciesManager(), getUser().getRosterManager());
         megaProgress = Math.min(megaProgress, megaThreshold);
      }
      myData.setTeamForStage(curTeam, getCurrentStage());
   }
   
   private void removeSpeciesFromTeam(String name) {
      TeamImpl curTeam = new TeamImpl(getCurrentTeam());
      curTeam.removeName(name);
      myData.setTeamForStage(curTeam, getCurrentStage());
   }
   
   private void clearTeam() {
      Team curTeam = myData.getTeamForStage(getCurrentStage(), false);
      if (curTeam == null || !curTeam.getNames().isEmpty()) {
         // Forbids it from needing fallbacks
         myData.setTeamForStage(new TeamImpl(), getCurrentStage());
      } else {
         // Forces it to require fallbacks
         myData.removeTeamForStage(getCurrentStage());
      }
      updateTeamPanel();
   }
   
   private String getMegaSlot() {
      String chosen = megaChooser.getItemAt(megaChooser.getSelectedIndex());
      return chosen == null || chosen.equals(getString(KEY_NONE)) ? null : chosen;
   }
   
   private Effect getEffect() {
      return effectFilter.getSelectedEffect();
   }
   
   private boolean getMegaFilter() {
      return megaFilter.isSelected();
   }
   
   private PkmType getType() {
      return typeChooser.getSelectedType();
   }
   
   private String getContainsString() {
      return textField.getText();
   }
   
   private Integer getLevel() {
      return (Integer) levelSpinner.getValue();
   }
   
   @Override
   public String getTextFor(Object o) {
      return getUser().getTextFor(o);
   }
   
   @Override
   public ImageManager getImageManager() {
      return getUser().getImageManager();
   }
   
   @Override
   public Stage getCurrentStage() {
      return curStage;
   }
   
   @Override
   public Collection<Stage> getAllStages() {
      return getUser().getAllStages();
   }
   
   @Override
   public void setCurrentStage(Stage stage) {
      prevTeam = myData.getTeamForStage(curStage);
      curStage = stage;
      updateTeamPanel();
   }
   
   /**
    * @return
    */
   private List<Predicate<Species>> getCurrentFilters(boolean ignoreLevel) {
      List<Predicate<Species>> filters = getBasicFilters();
      PkmType type = getType();
      if (type != null) {
         filters.add(species -> species.getType().equals(type));
      }
      if (!ignoreLevel) {
         Integer curLevelFilter = getLevel();
         int minLevel = curLevelFilter != null ? curLevelFilter : 0;
         RosterManager rosterManager = getUser().getRosterManager();
         filters.add(species -> rosterManager.getLevelForSpecies(species.getName()) >= minLevel);
      }
      String str = getContainsString().toUpperCase();
      if (str != null && !str.isEmpty()) {
         filters.add(species -> species.getName().toUpperCase().contains(str));
      }
      Effect effect = getEffect();
      if (effect != null) {
         filters.add(species -> species.getEffect().equals(effect));
      }
      if (getMegaFilter()) {
         filters.add(species -> species.getMegaName() != null && !species.getMegaName().isEmpty());
      }
      return filters;
   }
   
   /**
    * @return
    */
   private List<Predicate<Species>> getBasicFilters() {
      List<Predicate<Species>> filters = new ArrayList<Predicate<Species>>();
      filters.add(species -> species.getEffect().canLevel());
      return filters;
   }
   
   /*
    * (non-Javadoc)
    * @see shuffle.fwk.config.provider.PreferencesManagerProvider#getPreferencesManager()
    */
   @Override
   public ConfigManager getPreferencesManager() {
      return getUser().getPreferencesManager();
   }
   
}
