package Utilities;

public class Parser {
    // int opcode; // bits31:26
    // int immediate; // bits15:0
    // int ALUOp;
    // int RegDst;
    // int ALUSrc;
    // int RegWrite;
    // int MemRead;
    // int MemWrite;
    // int Branch;
    // int MemToReg;

    public static int parse(String strInstruction) {
        String[] arrInstruction = strInstruction.split(" ");
        String op = arrInstruction[0];
        int instruction = 0;

        String type = "I";

        switch (op) {
            case "ADD":
                instruction = 0;
                type = "R";
                break;
            case "SUB":
                instruction = 1;
                type = "R";
                break;
            case "MULI":
                instruction = 2;
                break;
            case "ADDI":
                instruction = 3;
                break;
            case "BNE":
                instruction = 4;
                break;
            case "ANDI":
                instruction = 5;
                break;
            case "ORI":
                instruction = 6;
                break;
            case "J":
                instruction = 7;
                type = "J";
                break;
            case "SLL":
                instruction = 8;
                type = "R";
                break;
            case "SLR":
                instruction = 9;
                type = "R";
                break;
            case "LW":
                instruction = 10;
                break;
            case "SW":
                instruction = 11;
                break;
        }

        if (type.equals("J")) {
            int address = Integer.parseInt(arrInstruction[1]);
            instruction = instruction << 28;
            instruction = instruction | address;
            return instruction;
        }

        int rd = Integer.parseInt(arrInstruction[1].substring(1));
        int rs = Integer.parseInt(arrInstruction[2].substring(1));

        instruction = instruction << 5;
        instruction = instruction | rd;
        instruction = instruction << 5;
        instruction = instruction | rs;

        if (type.equals("R")) {
            if (op.equals("SLL") || op.equals("SRL")) {
                // Set RT to 0
                instruction = instruction << 5;

                // Set Shift Amount
                int shiftAmount = Integer.parseInt(arrInstruction[3]);
                instruction = instruction << 13;
                instruction = instruction | shiftAmount;
            } else {
                // Set RT
                int rt = Integer.parseInt(arrInstruction[3].substring(1));
                instruction = instruction << 5;
                instruction = instruction | rt;

                // Set Shift Amount to 0
                instruction = instruction << 13;
            }
        } else {
            // Set Immediate
            int immediate = Integer.parseInt(arrInstruction[3]);
            // mask immediate to only the 18 lsb
            immediate = immediate & 0x0003FFFF;
            instruction = instruction << 18;
            instruction = instruction | immediate;
        }
        return instruction;
    }

    public static String toBinary(int x) {
        StringBuilder str = new StringBuilder(String.format("%32s", Integer.toBinaryString(x)).replaceAll(" ", "0"));
        // add a space after 4 bits then after 5 bits 3 times
        str.insert(4, " ");
        str.insert(10, " ");
        str.insert(16, " ");
        str.insert(22, " ");
        str.insert(28, " ");
        return str.toString();
    }

    public static void main(String[] args) {
        String inst1 = "ADD R1 R2 R3";
        String inst2 = "ADDI R1 R2 10";
        String inst3 = "BNE R1 R2 10";
        String inst4 = "J 10";
        String inst5 = "SLL R1 R2 10";

        Parser parser = new Parser();

        System.out.println(toBinary(parser.parse(inst5)));
    }
}
