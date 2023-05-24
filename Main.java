import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;

import Utilities.*;

public class Main {

   int[] memory = new int[2048];
   int registers[] = new int[32];
   int pc;

   Hashtable<String, Integer> fetchDecodeReg = new Hashtable<String, Integer>();
   Hashtable<String, Integer> fetchDecodeRegTemp = new Hashtable<String, Integer>();
   Hashtable<String, Integer> decodeExecuteReg = new Hashtable<String, Integer>();
   Hashtable<String, Integer> decodeExecuteRegTemp = new Hashtable<String, Integer>();
   Hashtable<String, Integer> executeMemReg = new Hashtable<String, Integer>();
   Hashtable<String, Integer> executeMemRegTemp = new Hashtable<String, Integer>();
   Hashtable<String, Integer> memWBReg = new Hashtable<String, Integer>();
   Hashtable<String, Integer> memWBRegTemp = new Hashtable<String, Integer>();

   boolean isBranching = false;

   int decodeRepeat = 0;
   int decodeCurrent = -1;

   int executeRepeat = 0;
   int executeCurrent = -1;

   int memoryCurrent = -1;

   int wbCurrent = -1;

   public Main() {
      pc = 0;
   }

   public void run() {
      while (true) {
         int instruction = memory[pc];
         int opcode = instruction >>> 28;
         int rs = (instruction >>> 21) & 0x1F;
         int rt = (instruction >>> 16) & 0x1F;
         int rd = (instruction >>> 11) & 0x1F;
         int shamt = (instruction >>> 6) & 0x1F;
         int funct = instruction & 0x3F;
         int immediate = instruction & 0xFFFF;
         int address = instruction & 0x3FFFFFF;

         switch (opcode) {
            case 0:
               switch (funct) {
                  case 0:
                     registers[rd] = registers[rs] + registers[rt];
                     break;
                  case 1:
                     registers[rd] = registers[rs] - registers[rt];
                     break;
                  case 2:
                     registers[rd] = registers[rs] * registers[rt];
                     break;
                  case 3:
                     registers[rd] = registers[rs] + immediate;
                     break;
                  case 4:
                     if (registers[rs] != registers[rt]) {
                        pc = pc + immediate;
                     }
                     break;
                  case 5:
                     registers[rd] = registers[rs] & immediate;
                     break;
                  case 6:
                     registers[rd] = registers[rs] | immediate;
                     break;
                  case 7:
                     pc = address;
                     break;
                  case 8:
                     registers[rd] = registers[rs] << shamt;
                     break;
                  case 9:
                     registers[rd] = registers[rs] >> shamt;
                     break;
               }
               break;
            case 1:
               registers[rt] = registers[rs] + immediate;
               break;
            case 2:
               registers[rt] = registers[rs] & immediate;
               break;
            case 3:
               registers[rt] = registers[rs] | immediate;
               break;
            case 4:
               pc = address;
               break;
            case 5:
               registers[rt] = registers[rs] << shamt;
               break;
            case 6:
               registers[rt] = registers[rs] >> shamt;
               break;
            case 7:
               registers[rt] = memory[registers[rs] + immediate];
               break;
            case 8:
               memory[registers[rs] + immediate] = registers[rt];
               break;
         }
      }
   }

   public int loadProgram(String path) {
      pc = 0;
      int numOfInst = 0;
      BufferedReader br;
      try {
         br = new BufferedReader(new FileReader(path));
         String line;
         // store the converted instructions immediately in the memory array
         while ((line = br.readLine()) != null) {
            if (pc > 1023) {
               System.out.println("Program too large for memory");
               return 0;
            }
            memory[pc] = Parser.parse(line);
            pc++;
         }

         br.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

      numOfInst = pc;
      pc = 0;
      return numOfInst;

   }

   public void fetch() {
      int currInstruction = memory[pc];
      if (pc == 1024) {
         System.out.println("You've fetched all instructions!");
         return;
      }

      pc++;
      System.out.println("\n[1] Fetch Stage  # Instruction " + (pc) + " #");
      System.out.println("\nPC has changed from " + (pc - 1) + " to " + pc);

      fetchDecodeRegTemp.put("instruction", currInstruction);
      fetchDecodeRegTemp.put("pc", pc);

      // print the outputs
      System.out.println("\nOutputs:\nInstruction: " + Parser.toBinary(currInstruction));
      System.out.println("PC: " + pc);

   }

   public void decode() {

      Integer currPc = fetchDecodeReg.get("pc");
      if (currPc == null) {
         fetchDecodeReg.putAll(fetchDecodeRegTemp);
         return;
      }
      // System.out.println("Decode: " + currPc + " equal to " + decodeCurrent);
      if (currPc != decodeCurrent) {
         // new instruction was fetched
         decodeCurrent = currPc;

         if (decodeRepeat == 0) {
            // this is the first time decoding this instruction
            decodeRepeat = 1;
            fetchDecodeReg.putAll(fetchDecodeRegTemp);
            return;
         }
         // else {
         // }
      }

      int instruction = fetchDecodeReg.get("instruction");
      System.out.println("\n[2] Decode Stage  # Instruction " + (currPc) + " #");
      System.out.println("\nInputs:\nInstruction: " + Parser.toBinary(instruction));

      int opcode = instruction >>> 28;
      int rd = (instruction >>> 23) & 0x1F;
      int rs = (instruction >>> 18) & 0x1F;
      int rt = (instruction >>> 13) & 0x1F;
      int shamt = (instruction) & 0x1FFF;

      // the immediate is a 2's complement number so can be negative
      int immediate = instruction & 0x3FFFF;

      // if the immediate is negative, sign extend it
      if ((immediate & 0x20000) != 0) {
         immediate = immediate | 0xFFFC0000;
      }
      int address = instruction & 0xFFFFFFF;
      int rdValue = registers[rd];
      int rsValue = registers[rs];
      int rtValue = registers[rt];

      System.out.println("RD (R" + rd + "): " + registers[rd]);
      // print rdvalue

      // Write these values to the decodeExecuteReg hashtable
      // store pc
      decodeExecuteRegTemp.put("pc", currPc);
      decodeExecuteRegTemp.put("opcode", opcode);
      decodeExecuteRegTemp.put("shamt", shamt);
      decodeExecuteRegTemp.put("immediate", immediate);
      decodeExecuteRegTemp.put("address", address);
      decodeExecuteRegTemp.put("rdValue", rdValue);
      decodeExecuteRegTemp.put("rsValue", rsValue);
      decodeExecuteRegTemp.put("rtValue", rtValue);
      decodeExecuteRegTemp.put("rd", rd);

      // print all outputs
      System.out.println("\nOutputs:");
      System.out.println("Opcode: " + opcode);
      System.out.println("RD (R" + rd + ") Value:" + rdValue);
      System.out.println("Read Data 1 " + rsValue);
      System.out.println("Read Data 2 " + rtValue);
      System.out.println("Shamt: " + shamt);
      System.out.println("Immediate: " + immediate);
      System.out.println("Address: " + address);

      // Reset decodeRepeat
      decodeRepeat = 0;

      fetchDecodeReg.putAll(fetchDecodeRegTemp);

   }

   public void execute() {
      Integer currPc = decodeExecuteReg.get("pc");

      if (currPc == null) {
         decodeExecuteReg.putAll(decodeExecuteRegTemp);
         return;
      }

      if (currPc != executeCurrent) {
         // new instruction was fetched
         executeCurrent = currPc;

         if (executeRepeat == 0) {
            // this is the first time executing this instruction
            executeRepeat = 1;
            decodeExecuteReg.putAll(decodeExecuteRegTemp);
            return;
         }

      }

      System.out.println("\n[3] Execute Stage  # Instruction " + (currPc) + " #");

      int memRead = 0;
      int memWrite = 0;
      int output = 0;
      int rsValue = decodeExecuteReg.get("rsValue");
      int rtValue = decodeExecuteReg.get("rtValue");
      int rdValue = decodeExecuteReg.get("rdValue");
      int rd = decodeExecuteReg.get("rd");
      // print these inputs
      System.out.println("\nInputs:");
      System.out.println("PC: " + currPc);
      System.out.println("Read Data 1: " + rsValue);
      System.out.println("Read Data 2: " + rtValue);
      System.out.println("RD (R" + rd + ") Value: " + rdValue);
      System.out.println("Opcode: " + decodeExecuteReg.get("opcode"));
      System.out.println("Shamt: " + decodeExecuteReg.get("shamt"));
      System.out.println("Immediate: " + decodeExecuteReg.get("immediate"));
      System.out.println("Address: " + decodeExecuteReg.get("address"));

      // Get opcode from decodeExecuteReg
      int opcode = decodeExecuteReg.get("opcode");
      // if R-type instruction (0/1/8/9) get rd, rs, rt, shamt from decodeExecuteReg
      if (opcode == 0 || opcode == 1 || opcode == 8 || opcode == 9) {
         int shamt = decodeExecuteReg.get("shamt");
         // switch and execute
         switch (opcode) {
            case 0:
               output = rsValue + rtValue;
               break;
            case 1:
               output = rsValue - rtValue;
               break;
            case 8:
               output = rsValue << shamt;
               break;
            case 9:
               output = rsValue >>> shamt;
               break;
         }
      } else if (opcode == 7) {
         // J type
         int address = decodeExecuteReg.get("address");
         // get pc from
         pc = (currPc & 0xF0000000) | address;
         isBranching = true;
      } else {
         // I type
         int immediate = decodeExecuteReg.get("immediate");
         switch (opcode) {
            case 2: // MULI
               output = rsValue * immediate;
               break;
            case 3: // ADDI
               output = rsValue + immediate;
               break;
            case 4: // BNE
               System.out.println("R" + rd + " in memory: " + registers[rd]);
               System.out.println("BNE: R" + rd + ": " + rdValue + " != " + rsValue);
               // IF(R1 != R2) {PC = PC+1+IMM }
               if (rdValue != rsValue) {
                  pc = currPc + immediate;
                  // print
                  System.out.println("\nSetting PC to " + pc);
                  isBranching = true;
               }
               break;
            case 5: // ANDI
               output = rsValue & immediate;
               break;
            case 6: // ORI
               output = rsValue | immediate;
               break;
            case 10: // LW
               output = rsValue + immediate;
               memRead = 1;
               memWrite = 0;
               break;
            case 11: // SW
               output = rsValue + immediate;
               memRead = 0;
               memWrite = 1;
               break;
         }
      }

      // set executeMemReg
      executeMemRegTemp.put("memRead", memRead);
      executeMemRegTemp.put("memWrite", memWrite);
      executeMemRegTemp.put("output", output);
      executeMemRegTemp.put("rdValue", rdValue);
      executeMemRegTemp.put("opcode", opcode);
      executeMemRegTemp.put("rd", rd);
      executeMemRegTemp.put("pc", currPc);

      // print outputs
      System.out.println("\nOutputs:");
      System.out.println("PC: " + currPc);
      System.out.println("MemRead: " + memRead);
      System.out.println("MemWrite: " + memWrite);
      System.out.println("ALU Output: " + output);
      System.out.println("RD (R" + rd + ") Value: " + rdValue);
      // opcode and pc
      System.out.println("Opcode: " + opcode);

      // Reset executeRepeat
      executeRepeat = 0;

      decodeExecuteReg.putAll(decodeExecuteRegTemp);
   }

   public void memory() {
      Integer currPc = executeMemReg.get("pc");

      if (currPc == null || currPc == memoryCurrent) {
         executeMemReg.putAll(executeMemRegTemp);
         return;
      }

      // new instruction was executed
      memoryCurrent = currPc;

      System.out.println("[4] Memory Stage  # Instruction " + (currPc) + " #");

      int memRead = executeMemReg.get("memRead");
      int memWrite = executeMemReg.get("memWrite");
      int address = executeMemReg.get("output");
      int opcode = executeMemReg.get("opcode");
      int rd = executeMemReg.get("rd");
      // print the inputs like above
      System.out.println("\nInputs: ");
      // pc
      System.out.println("PC: " + currPc);
      System.out.println("MemRead: " + memRead);
      System.out.println("MemWrite: " + memWrite);
      System.out.println("Address: " + address);
      System.out.println("RD (R" + rd + ") Value: " + executeMemReg.get("rdValue"));
      System.out.println("Opcode: " + opcode);

      int readData = 0;

      if (memRead == 1) {
         readData = memory[address];
      } else if (memWrite == 1) {
         memory[address] = executeMemReg.get("rdValue");
         System.out.println("Memory[" + address + "] = " + memory[address]);
      }

      // set memWBReg
      memWBRegTemp.put("readData", readData);
      memWBRegTemp.put("aluOutput", address);
      memWBRegTemp.put("pc", currPc);
      memWBRegTemp.put("opcode", opcode);
      memWBRegTemp.put("rd", rd);

      // print outputs
      System.out.println("\nOutputs:");
      System.out.println("PC: " + currPc);
      System.out.println("Read Data: " + readData);
      System.out.println("ALU Output: " + address);
      System.out.println("RD (R" + rd + ") Value: " + executeMemReg.get("rdValue"));
      System.out.println("Opcode: " + opcode);

      executeMemReg.putAll(executeMemRegTemp);

   }

   public void writeBack() {
      Integer currPc = memWBReg.get("pc");

      if (currPc == null || currPc == wbCurrent) {
         memWBReg.putAll(memWBRegTemp);
         return;
      }

      // new instruction was in memory
      wbCurrent = currPc;

      System.out.println("\n[5] Write Back Stage  # Instruction " + (currPc) + " #");

      int readData = memWBReg.get("readData");
      int aluOutput = memWBReg.get("aluOutput");
      int opcode = memWBReg.get("opcode");
      int rd = memWBReg.get("rd");

      // print inputs
      System.out.println("\nInputs:");
      System.out.println("PC: " + currPc);
      System.out.println("Read Data: " + readData);
      System.out.println("ALU Output: " + aluOutput);
      System.out.println("Opcode: " + opcode);
      System.out.println("RD : R" + rd);

      if (opcode == 4 || opcode == 7 || opcode == 11) {
         // will not write back
      } else if (opcode == 10) {
         // LW
         int toStore = rd == 0 ? 0 : readData;
         registers[rd] = toStore;
         System.out.println("\nSetting R" + rd + " = " + toStore);
      } else {
         // all other instructions
         int toStore = rd == 0 ? 0 : aluOutput;
         registers[rd] = toStore;
         System.out.println("\nSetting R" + rd + " = " + toStore);
      }

      memWBReg.putAll(memWBRegTemp);

   }

   public static void main(String[] args) {
      Main main = new Main();
      int numOfInst = main.loadProgram("program.txt");

      for (int i = 0; i < 9; i++) {
         System.out.println(Parser.toBinary(main.memory[i]));
      }

      System.out.println("Number of instructions: " + numOfInst);

      // int loop = 7 + (numOfInst - 1) * 2;
      int nSoFar = 0;
      for (int i = 1; i <= (7 + (nSoFar - 1) * 2); i++) {
         main.isBranching = false;
         System.out.println("\n========================");
         System.out.println("        Cycle " + i);
         System.out.println("========================");

         if (i % 2 == 1) {
            if (main.pc < numOfInst) {
               nSoFar++;
            }
            main.fetch();
            System.out.println();
         }
         main.decode();
         System.out.println();
         main.execute();
         System.out.println();
         // if branching
         main.memory();
         main.writeBack();

         if (main.isBranching) {
            main.isBranching = false;
            main.resetAllVariables();
         }

         System.out.println();

      }

      System.out.println("\n============= END =============\n");

      // print all registers and their values
      System.out.println("Registers:");
      // pc
      System.out.println("PC: " + main.pc);
      for (int i = 0; i < 32; i++) {
         System.out.println("R" + i + ": " + main.registers[i]);
      }

      System.out.println();

      // print all memory addresses and their values

      System.out.println("Memory:\n");
      System.out.println("Instructions:");
      for (int i = 0; i < 2048; i++) {
         if (i < 1024)
            System.out.println("[" + i + "]: " + Parser.toBinary(main.memory[i]));
         else
            System.out.println("[" + i + "]: " + main.memory[i]);
         if (i == 1023)
            System.out.println("\nData:");
      }
   }

   private void resetAllVariables() {
      fetchDecodeReg.clear();
      fetchDecodeRegTemp.clear();
      decodeExecuteReg.clear();
      decodeExecuteRegTemp.clear();
      executeMemReg.clear();
      executeMemRegTemp.clear();
      memWBReg.clear();
      memWBRegTemp.clear();

      decodeRepeat = 0;
      decodeCurrent = -1;

      executeRepeat = 0;
      executeCurrent = -1;

      memoryCurrent = -1;

      wbCurrent = -1;
   }
}
