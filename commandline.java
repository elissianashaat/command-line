import java.io.*;
import java.nio.file.*;
import java.util.*;
import static java.nio.file.Files.lines;
import java.util.Collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

class Parser {
    public List<String> hist = new ArrayList<>();
    String commandName = ""; // Initialize with a default value
    String[] args;

    // Method to parse the input command
    public boolean parse(String input) {
        String[] arrOfStr = input.split(" ");
        // Check if there are more than one element in the input
        if (arrOfStr.length > 1) {
            // Check if the command is "ls -r" or "cp -r"
            if ((arrOfStr[0].equals("ls") && arrOfStr[1].equals("-r")) || (arrOfStr[0].equals("cp") && arrOfStr[1].equals("-r"))) {
                // Set the commandName to the command name and arguments combined
                commandName = arrOfStr[0] + " " + arrOfStr[1];
            } else {
                // Set the commandName to the first element
                commandName = arrOfStr[0];
            }

            // Initialize the args array with a size equal to the length of arrOfStr minus one
            args = new String[arrOfStr.length - 1];

            // Copy the arguments from arrOfStr into args
            System.arraycopy(arrOfStr, 1, args, 0, args.length);
        } else {
            // If there's only one element, set it as the command name
            commandName = arrOfStr[0];

            // Initialize the args array with zero elements
            args = new String[0]; // No arguments
        }

        return true; // Return true to indicate successful parsing
    }

    // Method to get the parsed command name
    public String getCommandName() {
        return commandName;
    }

    // Method to get the parsed arguments
    public String[] getArgs() {
        return args;
}
}
class Terminal {
    Parser parser;
    String CurrentDirectory;

    public Terminal() {
        parser = new Parser();
        CurrentDirectory = System.getProperty("user.dir");
    }

    // Implement each command in a method, for example:
    public String pwd() {
        // Get the current working directory and return it as a string
        return CurrentDirectory;
    }

    public void echo() {
        String OutPut = String.join(" ", parser.getArgs());
        System.out.println(OutPut);
    }

    public void cd(String[] aftercd) {
        if (aftercd.length == 0) {
            String home = System.getProperty("user.home");
            CurrentDirectory = home;
            System.setProperty("user.dir", CurrentDirectory);
        } else if (aftercd[0].equals("..")) {
            File current = new File(CurrentDirectory);
            File parentDirectory = current.getParentFile();
            if (parentDirectory != null) {
                CurrentDirectory = parentDirectory.getAbsolutePath();
                System.setProperty("user.dir", CurrentDirectory);
            }
        } else {
            File absolute = new File(aftercd[0]);
            if (!absolute.isAbsolute()) {
                absolute = new File(CurrentDirectory, aftercd[0]);
            }
            if (absolute.exists() && absolute.isDirectory()) {
                CurrentDirectory = absolute.getAbsolutePath();
                System.setProperty("user.dir", CurrentDirectory);
            }
        }
    }

    public void ls(String[] args) {
        String[] files;
        File folder = new File(CurrentDirectory);
        files = folder.list();

        if (files != null) {
            if (args.length > 0 && args[0].equals("-r")) {
                Arrays.sort(files, Collections.reverseOrder());
                for (String file : files) {
                    System.out.println(file);

                }
            } else if (args.length == 0){
                Arrays.sort(files);
                for (String file : files) {
                    System.out.println(file);
                }
            }
            else {
                System.out.println("Wrong command");
            }

        }
    }


    public void touch(String[] args) {
        if (args.length != 1) {
            System.out.println("error. This function only accepts one argument");
            return;
        }
        Path filePath = Paths.get(CurrentDirectory, args[0]);
        try {
            Files.createFile(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void mkdir(String[] args) {
        for (String arg : args) {
            File dir = new File(arg);
            if (!dir.isAbsolute()) {
                dir = new File(CurrentDirectory, arg);
            }
            if (dir.exists()) {
                System.out.println("Directory " + arg + " already exists");
            } else {
                boolean done = dir.mkdir();
                if (done) {
                    System.out.println("Directory " + arg + " successfully created");
                } else {
                    System.out.println("Failed to create " + arg + " directory");
                }
            }
        }
    }
    public void history() {
        int i=0;
        for (String s : parser.hist) {
            System.out.println((i + 1) + " " + s);
            i++;
        }

    }

    public void rmdir(String[] arg) {
        if (arg.length != 1) {
            System.out.println("error. This function only accepts one argument");
            return;
        }
        if (arg[0].equals("*")) {
            File dir = new File(CurrentDirectory);
            if (dir.listFiles().length == 0) {
                System.out.println("There are no directories to delete");
                return;
            }
            for (File temp : dir.listFiles()) {
                if (temp.isDirectory()) {
                    temp.delete();
                }
            }
            System.out.println("The directories were deleted successfully");
        } else {
            File dir = new File(arg[0]);
            if (!dir.isAbsolute()) {
                dir = new File(CurrentDirectory, arg[0]);
            }
            if (dir.exists() && dir.isDirectory() && dir.delete()) {
                System.out.println("The directory was deleted successfully");
            } else {
                System.out.println("Error. Failed to delete the directory");
            }
        }
    }

    public void rm(String[] filename) {
        if (filename.length != 1) {
            System.out.println("error. This function only accepts one argument");
            return;
        }
        Path filePath = Paths.get(CurrentDirectory, filename[0]);
        if (Files.exists(filePath)) {
            try {
                Files.delete(filePath);
                System.out.println("File deleted successfully.");
            } catch (IOException e) {
                System.out.println("Error deleting the file: " + e.getMessage());
            }
        } else {
            System.out.println("File isn't available in the current directory");
        }
    }

    public void cat(String... filenames) {
        for (String filename : filenames) {
            Path filePath = Paths.get(CurrentDirectory, filename);
            try {
                lines(filePath).forEach(System.out::println);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // This command "cp" is supposed to copy the content of the first file into the second file
    public void cp(String[] args) {
        // Condition to ensure 2 arguments only
        if (args.length != 2) {
            System.out.println("Command Format: cp <file1.txt> <file2.txt>");
            return;
        }
        // Reserves places for the 2 files in the args array
        String file1 = args[0];
        String file2 = args[1];
        // Using Path to provide the full path of the current directory by creating an object
        Path file1path = Paths.get(CurrentDirectory, file1);
        Path file2path = Paths.get(CurrentDirectory, file2);
        // A condition to ensure displaying an error if the file doesn't exist
        if (!Files.exists(file1path)) {
            System.out.println("The file to be copied does not exist!");
            return;
        }
        if (!Files.exists(file2path)) {
            System.out.println("The file to be copied to (destination) does not exist");
            return;
        }
        // We will copy the content of the files through the paths with a try and catch to handle any errors
        try {
            Files.copy(file1path, file2path, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File is copied successfully!");
        } catch (IOException e) {
            System.err.println("Error while the file was being copied: " + e.getMessage());
        }
    }

    // This method is used to count words, lines, and characters in a file input
    public void wc(String[] args) {
        // A condition to check there is only one input
        if (args.length != 1) {
            System.out.println("Command format: wc <file.txt>");
            return;
        }
        String file1 = args[0];
        Path file1Path = Paths.get(CurrentDirectory, file1);

        // To check if the file does not exist
        if (!Files.exists(file1Path)) {
            System.out.println("This file does not exist!");
            return;
        }

        int wordCount = 0;
        int lineCount = 0;
        int charCount = 0;

        // Using BufferedReader to read the file line by line
        try (BufferedReader reader = Files.newBufferedReader(file1Path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                charCount += line.length();
                String[] words = line.split("\\s+");
                wordCount += words.length;
            }
            System.out.printf("%6d %6d %6d %s%n", lineCount, wordCount, charCount, file1);
        } catch (IOException e) {
            System.err.println("An error occurred while reading the file: " + e.getMessage());
        }
    }

    // This method will choose the suitable command method to be called
    public void chooseCommandAction() {
        parser.hist.add(parser.getCommandName());
        String[] commandParts = parser.getCommandName().split(" ");
        String actualCommand = commandParts[0];
        switch (parser.getCommandName()) {
            case "pwd":
                System.out.println(pwd());
                break;
            case "cd":
                cd(parser.getArgs());
                break;
            case "ls":
                ls(parser.getArgs());
                break;
            case "ls -r":
                ls(parser.getArgs());
                break;
            case "mkdir":
                mkdir(parser.getArgs());
                break;
            case "rmdir":
                rmdir(parser.getArgs());
                break;
            case "echo":
                echo();
                break;
            case "touch":
                touch(parser.getArgs());
                break;
            case "rm":
                rm(parser.getArgs());
                break;
            case "cat":
                cat(parser.getArgs());
                break;
            case "cp":
                cp(parser.getArgs());
                break;
            case "wc":
                wc(parser.getArgs());
                break;
            case "history":
                history();
                break;
            default:
                System.out.println("Enter a valid command.");
                break;
        }
    }

    public void exit() {
        System.exit(0);
    }

    public static void main(String[] args) {
        Terminal terminal = new Terminal();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Enter a command: ");
            String userInput = scanner.nextLine();

            if (userInput.equals("exit")) {
                break;
            }

            // Process the user input
            terminal.parser.parse(userInput);
            terminal.chooseCommandAction();
        }
    }
}