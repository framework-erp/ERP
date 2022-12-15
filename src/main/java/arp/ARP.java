package arp;

import arp.enhance.ClassEnhancer;
import arp.enhance.ClassParseResult;

public class ARP {
	public static void start(String... pkgs) throws Exception {
		ClassParseResult parseResult = ClassEnhancer.parseAndEnhance(pkgs);
	}
}
