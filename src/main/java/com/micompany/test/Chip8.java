/*
* Autor: Cris-tian-B
* Fecha: 9/26/2025
* Interprete Chip-8
* V0.9.0
*/
package com.micompany.test;

public class Chip8 {
    public static CPU cpu;

    public Chip8() {
        cpu = new CPU();
    }

    public static void cycleCpu() {
        cpu.executeCycle();
    }

    public static int getPixel(int i) {
        return cpu.getDisplayValue(i);
    }

    public static void setKeyValue(int i, int e){
        cpu.getKeyValue(i, e);
    }

}