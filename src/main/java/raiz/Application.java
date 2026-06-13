package raiz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import raiz.componentes.InsertorDatos;

import java.util.Scanner;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class Application implements CommandLineRunner {

    private final InsertorDatos insertor;

    public Application(InsertorDatos insertor) {
        this.insertor = insertor;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Scanner sc = new Scanner(System.in);
        int opcion;

        do {
            System.out.println("\n1_Cargar excel \n2_Finalizar programa \n");
            System.out.print("Seleccione una opción: ");
            opcion = sc.nextInt();
            sc.nextLine();

            switch (opcion) {
                case 1:
                    System.out.println("Ingrese el path del archivo excel:");
                    String rutaArchivo = sc.nextLine();

                    insertor.importarDesdeExcel(rutaArchivo);
                    break;

                case 2:
                    System.out.println("Finalizando programa...");
                    break;

                default:
                    System.out.println("Respuesta inválida, elija un número dentro del rango esperado.");
                    break;
            }

        } while (opcion != 2);

        sc.close();
    }
}