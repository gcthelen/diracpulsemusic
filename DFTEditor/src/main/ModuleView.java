package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JComponent;

import main.Module.Connector;
import main.ModuleEditor.ModuleScreenInfo;


public class ModuleView extends JComponent {
	
	private static final long serialVersionUID = -443010057744196001L;

	private ModuleEditor parent = null;
	
	ModuleView(ModuleEditor parent) {
		this.parent= parent;
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		ArrayList<ModuleScreenInfo> moduleScreenInfoArray = parent.getModulesScreenInfo();
		for(ModuleScreenInfo moduleScreenInfo: moduleScreenInfoArray) {
			moduleScreenInfo.module.draw(g, moduleScreenInfo.dimensions.x, moduleScreenInfo.dimensions.y);
		}
		for(long connectorID: parent.outputToInput.keySet()) {
			int x0 = (int) Math.round(parent.connectorIDToConnector.get(connectorID).getSelectArea().getCenterX());
			int y0 = (int) Math.round(parent.connectorIDToConnector.get(connectorID).getSelectArea().getCenterY());
			long connectorID1 = parent.outputToInput.get(connectorID);
			int x1 = (int) Math.round(parent.connectorIDToConnector.get(connectorID1).getSelectArea().getCenterX());
			int y1 = (int) Math.round(parent.connectorIDToConnector.get(connectorID1).getSelectArea().getCenterY());			
			int red = (int) connectorID % 128;
			int green = (int) (connectorID / 128) % 128;
			int blue = (int) (connectorID / (128 * 128)) % 128;
			g2.setColor(new Color(red + 128, green + 128, blue + 128, 128));
			g2.setStroke(new BasicStroke(4));
			g2.drawLine(x0, y0, x1, y1);
		}
		if(parent.selectedOutput != null) {
			long connectorID1 = parent.selectedOutput;
			int x1 = (int) Math.round(parent.connectorIDToConnector.get(connectorID1).getSelectArea().getCenterX());
			int y1 = (int) Math.round(parent.connectorIDToConnector.get(connectorID1).getSelectArea().getCenterY());
			int red = (int) connectorID1 % 128;
			int green = (int) (connectorID1 / 128) % 128;
			int blue = (int) (connectorID1 / (128 * 128)) % 128;
			g2.setColor(new Color(red + 128, green + 128, blue + 128, 128));
			g2.setStroke(new BasicStroke(4));
			g2.drawLine(parent.controller.getMouseX(), parent.controller.getMouseY(), x1, y1);
		}
	}
 
}