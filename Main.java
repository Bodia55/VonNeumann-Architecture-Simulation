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
      System.out.println("Fetch Stage: Instruction " + pc);
      System.out.println("PC has changed from " + (pc - 1) + " to " + pc);

      fetchDecodeRegTemp.put("instruction", currInstruction);
      fetchDecodeRegTemp.put("pc", pc);

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

      // if (decodeRepeat == 0) {
      // // first time decoding this instruction
      // decodeRepeat = 1;
      // fetchDecodeReg.putAll(fetchDecodeRegTemp);
      // return;
      // }

      int instruction = fetchDecodeReg.get("instruction");
      int opcode = instruction >>> 28;
      int rd = (instruction >>> 23) & 0x1F;
      int rs = (instruction >>> 18) & 0x1F;
      int rt = (instruction >>> 13) & 0x1F;
      int shamt = (instruction) & 0x1FFF;
      int immediate = instruction & 0x3FFFF;
      int address = instruction & 0xFFFFFFF;

      System.out.println("Decode Stage: Instruction " + (fetchDecodeReg.get("pc")));
      // Write these values to the decodeExecuteReg hashtable
      // store pc
      decodeExecuteRegTemp.put("pc", currPc);
      decodeExecuteRegTemp.put("opcode", opcode);
      decodeExecuteRegTemp.put("shamt", shamt);
      decodeExecuteRegTemp.put("immediate", immediate);
      decodeExecuteRegTemp.put("address", address);
      decodeExecuteRegTemp.put("rdValue", registers[rd]);
      decodeExecuteRegTemp.put("rsValue", registers[rs]);
      decodeExecuteRegTemp.put("rtValue", registers[rt]);

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
         // else {
         // }
      }

      // if (executeRepeat == 0) {
      // // first time decoding this instruction
      // executeRepeat = 1;
      // decodeExecuteReg.putAll(decodeExecuteRegTemp);
      // return;
      // }

      System.out.println("Execute Stage: Instruction " + (currPc));

      int memRead = 0;
      int memWrite = 0;
      int output = 0;
      int rsValue = registers[decodeExecuteReg.get("rsValue")];
      int rtValue = registers[decodeExecuteReg.get("rtValue")];
      int rdValue = registers[decodeExecuteReg.get("rdValue")];

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
               // IF(R1 != R2) {PC = PC+1+IMM }
               if (rdValue != rsValue) {
                  pc = currPc + immediate;
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
               executeMemReg.put("address", rsValue + immediate);
               memRead = 0;
               memWrite = 1;
               break;
         }
      }

      // set executeMemReg
      executeMemReg.put("memRead", memRead);
      executeMemReg.put("memWrite", memWrite);
      executeMemReg.put("output", output);
      executeMemReg.put("pc", currPc);

      // Reset executeRepeat
      executeRepeat = 0;

      decodeExecuteReg.putAll(decodeExecuteRegTemp);
   }

   public static void main(String[] args) {
      Main main = new Main();
      int numOfInst = main.loadProgram("program.txt");

      for (int i = 0; i < 9; i++) {
         System.out.println(Parser.toBinary(main.memory[i]));
      }

      int loop = 7 + (numOfInst - 1) * 2;
      for (int i = 1; i <= loop; i++) {
         System.out.println("Cycle " + i);
         if (i % 2 == 1)
            main.fetch();
         main.decode();
         main.execute();
         System.out.println();
      }
   }
}
