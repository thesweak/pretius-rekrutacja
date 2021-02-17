package zadanie;

public class Main {

    public static void main(String[] args) {
        Directory directory = new Directory();
        directory.createDirectoryStructure();

        directory.startDirectoryWatching();
    }
}