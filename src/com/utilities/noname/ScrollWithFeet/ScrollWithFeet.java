package com.utilities.noname.ScrollWithFeet;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.File;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

import java.util.Locale;
import javax.speech.Central;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;

public class ScrollWithFeet {

	Controller wheel = null;

	String paddelAxis = "Y-Achse";

	Robot robot;

	int profile = 1;

	public static void main(String[] args) {
		new ScrollWithFeet();
	}

	public ScrollWithFeet() {
		try {
			System.setProperty("net.java.games.input.librarypath",
					new File("eController/eController_lib/natives").getAbsolutePath());
			// Set property as Kevin Dictionary
			System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us" + ".cmu_time_awb.AlanVoiceDirectory");

			// Register Engine
			Central.registerEngineCentral("com.sun.speech.freetts" + ".jsapi.FreeTTSEngineCentral");
		}

		catch (Exception e) {
			e.printStackTrace();
		}

		getWheel();

		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}

		startWheelReader();
	}

	public void startWheelReader() {
		int profileSwitch = 0;

		float def = 0;

		Component[] components = wheel.getComponents();
		while (true) {
			wheel.poll();

			float pressedAmountNow = components[2].getPollData();

			if (def == 0)
				def = pressedAmountNow * -1;

			if (def != pressedAmountNow * -1) {
				double scroll = pressedAmountNow * -1;

				scroll = scroll * 50;

				for (int i = 0; i <= profile; i++) {
					robot.mouseWheel((int) scroll);
				}
			}

			for (Component c : components) {
				if (c.getIdentifier().getName().matches("^[0-9]*$")) { // If the component identifier name contains only

					boolean pressed = false;
					if (c.getPollData() != 0.0f) {
						profileSwitch += 10;
						pressed = true;
					}

					if (c.getIdentifier().getName().contains("5") && profileSwitch == 10 && pressed) {
						profile++;
						if (profile > 10)
							profile = 1;
						say(profile + "");
					}
				}
			}

			if (profileSwitch > 0) {
				profileSwitch -= 4;
				if (profileSwitch < 0)
					profileSwitch = 0;
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void getWheel() {
		Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();

		for (int i = 0; i < ca.length; i++) {
			if (ca[i].getName().equals("USB RACING WHEEL")) {
				wheel = ca[i];
			}
		}

		if (wheel == null) {
			System.out.println("Wheel not found");
			System.exit(0);
		} else
			System.out.println("Wheel found and connected");
	}

	public void say(String text) {
		Thread sayer = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Synthesizer synthesizer = Central.createSynthesizer(new SynthesizerModeDesc(Locale.ENGLISH));

					// Allocate synthesizer
					synthesizer.allocate();

					// Resume Synthesizer
					synthesizer.resume();

					// Speaks the given text
					// until the queue is empty.
					synthesizer.speakPlainText(text, null);
					synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
				} catch (Exception e) {
					// TODO: handle exception
				}

			}
		});
		sayer.start();
	}

}
