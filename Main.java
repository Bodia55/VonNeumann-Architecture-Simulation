import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import Utilities.*;

public class Main {

   int[] memory = new int[2048];
   int registers[] = new int[32];
   int pc;

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

   public void loadProgram(String path) {
      pc = 0;
      BufferedReader br;
      try {
         br = new BufferedReader(new FileReader(path));
         String line;
         // store the converted instructions immediately in the memory array
         while ((line = br.readLine()) != null) {
            if (pc > 1023) {
               System.out.println("Program too large for memory");
               return;
            }
            memory[pc] = Parser.parse(line);
            pc++;
         }
         br.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   public static void main(String[] args) {
      Main main = new Main();
      main.loadProgram("program.txt");

      for (int i = 0; i < 10; i++) {
         System.out.println(Parser.toBinary(main.memory[i]));
      }
   }
}
