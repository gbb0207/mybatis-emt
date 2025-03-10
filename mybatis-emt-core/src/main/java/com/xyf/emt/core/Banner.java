package com.xyf.emt.core;

/**
 * 启动时打印的banner
 */
public class Banner {

    public static void print() {
        System.out.println(
                        "  __  __      ___       _   _        ___ __  __ _____ \n" +
                        " |  \\/  |_  _| _ ) __ _| |_(_)______| __|  \\/  |_   _|\n" +
                        " | |\\/| | || | _ \\/ _` |  _| (_-<___| _|| |\\/| | | |  \n" +
                        " |_|  |_|\\_, |___/\\__,_|\\__|_/__/   |___|_|  |_| |_|  \n" +
                        "         |__/                                         \n");
    }

    public static void main(String[] args) {
        print();
    }
}
