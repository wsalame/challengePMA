package me.amp.challenge.main;

import me.amp.challenge.main.injector.AppModule;
import me.amp.challenge.main.injector.GuiceInjector;
import me.amp.challenge.main.service.InitServices;

public class Main {

	public static void main(String[] args) {
		try {
			GuiceInjector.getInstance().install(new AppModule());
			GuiceInjector.get(InitServices.class).startServices();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}