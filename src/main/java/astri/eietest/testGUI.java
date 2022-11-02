package astri.eietest;

import java.io.Serializable;
import java.util.Locale;

import javax.swing.SwingUtilities;

import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.util.MoreSystemProperties;

public class testGUI implements Serializable {
 
	public static void main(String[] args) {
		System.setProperty(MoreSystemProperties.HIDE_INFO_CUSTOMIZATIONS_TOOLS,"true");
		Locale.setDefault(new Locale("en", "US"));
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SwingCustomizer.getDefault().openObjectFrame(new AmcController());
			}
		});
	}
}
