package com.manuel.literalura.Principal;

import com.manuel.literalura.DTO.Datos;
import com.manuel.literalura.DTO.DatosAutor;
import com.manuel.literalura.DTO.DatosLibro;
import com.manuel.literalura.model.*;
import com.manuel.literalura.repository.AutorRepository;
import com.manuel.literalura.repository.LibroRepository;
import com.manuel.literalura.service.ConsumoAPI;
import com.manuel.literalura.service.ConvierteDatos;


import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private static final String URL_BASE = "https://gutendex.com/books/";
    private final AutorRepository repoAutor;
    private final LibroRepository repoLibro;
    private String json;


    public Principal(AutorRepository repoautor, LibroRepository repolibro){
        this.repoAutor= repoautor;
        this.repoLibro= repolibro;
    }

    private String menu = """
            --------------------------------------------
                         📚 MENÚ - LITERALURA 📚
            --------------------------------------------
            1 - Buscar Libros por tÍtulo en la web
            2 - Buscar Autor por su nombre
            3 - Listar Libros registrados
            4 - Listar autores registrados
            5 - Listar autores vivos en determinado año
            6 - Listar libros por idioma
            7 - Top 10 libros más buscados
            8 - Generar Estadísticas
            ----------------------------------------------
            0 - SALIR DEL PROGRAMA 🔚
            ----------------------------------------------
            Elija la opción a través de su número:
            """;

    public void mostrarMenu() {
        var opcion = -1;

        while (opcion != 0) {
            System.out.println(menu);
            try {
                opcion = Integer.valueOf(teclado.nextLine());
                switch (opcion) {
                    case 1:
                        buscarLibroPorTitulo();
                        break;
                    case 2:
                        buscarAutorPorNombre();
                        break;
                    case 3:
                        listarLibrosRegistrados();
                        break;
                    case 4:
                        listarAutoresRegistrados();
                        break;
                    case 5:
                        listarAutoresVivos();
                        break;
                    case 6:
                        listarLibrosPorIdioma();
                        break;
                    case 7:
                        top10Libros();
                        break;
                    case 8:
                        generarEstadisticas();
                        break;
                    case 0:
                        System.out.println("👋 Saliendo de la aplicación...");
                        break;
                    default:
                        System.out.println("Opción no válida!");
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Opción no válida: " + e.getMessage());

            }
        }
    }

    private DatosLibro getDatosLibros() {
        System.out.println("""
            --------------------------------
              BUSCAR LIBROS POR TÍTULO 📗
            --------------------------------
             """);
        System.out.println("🔍 Escribe el título del libro que deseas buscar:");
        String tituloNombre = teclado.nextLine();

        String url = URL_BASE + "?search=" + tituloNombre.replace(" ", "+");
        json = consumoAPI.obtenerDatos(url);

        Datos datos = conversor.obtenerDatos(json, Datos.class);

        //return datos.resultados().get(0); // Devolver solo el primer libro encontrado
        Optional<DatosLibro> libroBuscado = datos.resultados().stream()
                .filter(libro -> libro.titulo().toUpperCase().contains(tituloNombre.toUpperCase()))
                .findFirst();
        if(libroBuscado.isPresent()){
            return libroBuscado.get();
        }else{
            System.out.println("❌ No se encontró ningún libro con ese título en la API.");
            return null;
        }
    }

    public void buscarLibroPorTitulo() {
        DatosLibro datosLibro = getDatosLibros();

        if (datosLibro == null) return;

        try {
            Libro libro;
            DatosAutor datosAutor = datosLibro.autor().get(0);
            Autor autorExistente = repoAutor.buscarAutorPorNombre(datosAutor.nombre());
            if (autorExistente != null) {
                System.out.println("El autor ya existe");
                libro = new Libro(datosLibro, autorExistente);
            } else {
                Autor nuevoAutor = new Autor(datosAutor);
                libro = new Libro(datosLibro, nuevoAutor);
                repoAutor.save(nuevoAutor);
            }

            Libro libroBD = repoLibro.buscarLibroPorNombre(datosLibro.titulo());
            if (libroBD != null) {
                System.out.println("El libro ya está guardado en la BD.");
            } else {
                repoLibro.save(libro);
                System.out.println("****** LIBRO GUARDADO *******");
                System.out.println(libro.toString());

            }
        }catch (Exception e){
            System.out.println("Warning! " + e.getMessage());
        }
    }

    public void buscarAutorPorNombre () {
        System.out.println("""
                    -------------------------------
                        BUSCAR AUTOR POR NOMBRE 📗
                    -------------------------------
                    """);
        System.out.println("Ingrese el nombre del autor que deseas buscar:");
        var nombre = teclado.nextLine();
        Autor autor = repoAutor.buscarAutorPorNombre(nombre);
        if (autor != null) {
            System.out.println(
                    "\nAutor: " + autor.getNombre() +
                            "\nFecha de Nacimiento: " + autor.getNacimiento() +
                            "\nFecha de Fallecimiento: " + autor.getFallecimiento() +
                            "\nLibros: " + autor.getLibros().stream()
                            .map(l -> l.getTitulo()).collect(Collectors.toList()) + "\n"
            );
        } else {
            System.out.println("El autor no existe en la BD");
        }
    }

    public void listarLibrosRegistrados () {
        System.out.println("""
                    ----------------------------------
                       LISTAR LIBROS REGISTRADOS 📗
                    ----------------------------------
                     """);
        List<Libro> libros = repoLibro.buscarTodosLosLibros();
        libros.forEach(l -> System.out.println(
                "-------------- LIBRO \uD83D\uDCD9  -----------------" +
                        "\nTítulo: " + l.getTitulo() +
                        "\nAutor: " + l.getAutor().getNombre() +
                        "\nIdioma: " + l.getIdioma().getIdioma() +
                        "\nNúmero de descargas: " + l.getDescargas() +
                        "\n----------------------------------------\n"
        ));
    }

    public void listarAutoresRegistrados () {
        System.out.println("""
                    ----------------------------------
                       LISTAR AUTORES REGISTRADOS 📗 
                    ----------------------------------
                     """);
        List<Autor> autores = repoAutor.findAll();
        System.out.println();
        autores.forEach(l -> System.out.println(
                "Autor: " + l.getNombre() +
                        "\nFecha de Nacimiento: " + l.getNacimiento() +
                        "\nFecha de Fallecimiento: " + l.getFallecimiento() +
                        "\nLibros: " + l.getLibros().stream()
                        .map(t -> t.getTitulo()).collect(Collectors.toList()) + "\n"
        ));
    }

    public void listarAutoresVivos () {
        System.out.println("""
                    -----------------------------
                        LISTAR AUTORES VIVOS 📗
                    -----------------------------
                     """);
        System.out.println("Introduzca un año para verificar el autor(es) que desea buscar:");
        try {
            var fecha = Integer.valueOf(teclado.nextLine());
            List<Autor> autores = repoAutor.buscarAutoresVivos(fecha);
            if (!autores.isEmpty()) {
                System.out.println();
                autores.forEach(a -> System.out.println(
                        "Autor: " + a.getNombre() +
                                "\nFecha de Nacimiento: " + a.getNacimiento() +
                                "\nFecha de Fallecimiento: " + a.getFallecimiento() +
                                "\nLibros: " + a.getLibros().stream()
                                .map(l -> l.getTitulo()).collect(Collectors.toList()) + "\n"
                ));
            } else {
                System.out.println("No hay autores vivos en el año registrado");
            }
        } catch (NumberFormatException e) {
            System.out.println("Ingresa un año válido " + e.getMessage());
        }
    }

    public void listarLibrosPorIdioma() {
        System.out.println("""
                --------------------------------
                  LISTAR LIBROS POR IDIOMA 📗
                --------------------------------
                """);
        var menu = """
                    ---------------------------------------------------
                    Seleccione el idioma del libro que desea encontrar:
                    ---------------------------------------------------
                    1 - Español
                    2 - Francés
                    3 - Inglés
                    4 - Portugués
                    ----------------------------------------------------
                    """;
        System.out.println(menu);

        try {
            var opcion = Integer.parseInt(teclado.nextLine());

            switch (opcion) {
                case 1:
                    buscarLibrosPorIdioma("es");
                    break;
                case 2:
                    buscarLibrosPorIdioma("fr");
                    break;
                case 3:
                    buscarLibrosPorIdioma("en");
                    break;
                case 4:
                    buscarLibrosPorIdioma("pt");
                    break;
                default:
                    System.out.println("Opción inválida!");
                    break;
            }
        } catch (NumberFormatException e) {
            System.out.println("Opción no válida: " + e.getMessage());
        }
    }

    private void buscarLibrosPorIdioma(String idioma) {
        try {
            Idioma idiomaEnum = Idioma.valueOf(idioma.toUpperCase());
            List<Libro> libros = repoLibro.buscarLibrosPorIdioma(idiomaEnum);
            if (libros.isEmpty()) {
                System.out.println("No hay libros registrados en ese idioma");
            } else {
                System.out.println();
                libros.forEach(l -> System.out.println(
                        "--------------------- LIBRO  ----------------------" +
                                "\nTítulo: " + l.getTitulo() +
                                "\nAutor: " + l.getAutor().getNombre() +
                                "\nIdioma: " + l.getIdioma().getIdioma() +
                                "\nNúmero de descargas: " + l.getDescargas() +
                                "\n----------------------------------------\n"
                ));
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Introduce un idioma válido en el formato especificado.");
        }
    }


    public void top10Libros () {
        System.out.println("""
                    -------------------------------------
                         TOP 10 LIBROS MÁS BUSCADOS 📚
                    -------------------------------------
                     """);
        List<Libro> libros = repoLibro.top10Libros();
        System.out.println();
        libros.forEach(l -> System.out.println(
                "----------------- LIBRO  ----------------" +
                        "\nTítulo: " + l.getTitulo() +
                        "\nAutor: " + l.getAutor().getNombre() +
                        "\nIdioma: " + l.getIdioma().getIdioma() +
                        "\nNúmero de descargas: " + l.getDescargas() +
                        "\n-------------------------------------------\n"
        ));
    }
    public void generarEstadisticas () {
        System.out.println("""
                    ----------------------------
                       GENERAR ESTADÍSTICAS 📈
                    ----------------------------
                     """);
        var json = consumoAPI.obtenerDatos(URL_BASE);
        var datos = conversor.obtenerDatos(json, Datos.class);
        IntSummaryStatistics est = datos.resultados().stream()
                .filter(l -> l.descargas() > 0)
                .collect(Collectors.summarizingInt(DatosLibro::descargas));
        Integer media = (int) est.getAverage();
        System.out.println("\n--------- ESTADÍSTICAS  ------------");
        System.out.println("Media de descargas: " + media);
        System.out.println("Máxima de descargas: " + est.getMax());
        System.out.println("Mínima de descargas: " + est.getMin());
        System.out.println("Total registros para calcular las estadísticas: " + est.getCount());
        System.out.println("---------------------------------------------------\n");
    }
}
