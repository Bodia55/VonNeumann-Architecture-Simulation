package Utilities;

public class Parser {
    int opcode;    // bits31:26
    int immediate; // bits15:0
    int ALUOp;
    int RegDst;
    int ALUSrc;
    int RegWrite;
    int MemRead;
    int MemWrite;
    int Branch;
    int MemToReg;

    public void parse(String strInstruction) {
        String[] arrInstruction = strInstruction.split(" ");
        int instruction = 0b0;

        switch (arrInstruction[0])
        {
            case "ADD" -> instruction |= 0;
            case "SUB"-> instruction |= 1;
            case "MULI"-> instruction |= 2;
            case "ADDI"-> instruction |=3;
            case "BNE"-> instruction |= 4;
            case "ANDI"-> instruction |= 5;
            case "ORI"-> instruction |= 6;
            case "J"-> instruction |=7;
            case "SLL"-> instruction |= 8;
            case "SLR"-> instruction = 9;
            case "LW"-> instruction |= 10;
            case "SW" -> instruction |= 11;
        }




    }
}
