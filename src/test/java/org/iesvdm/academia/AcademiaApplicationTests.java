package org.iesvdm.academia;

import org.iesvdm.academia.modelo.Alumno;
import org.iesvdm.academia.modelo.Curso;
import org.iesvdm.academia.modelo.Matricula;
import org.iesvdm.academia.repositorio.AlumnoRepository;
import org.iesvdm.academia.repositorio.CursoRepository;
import org.iesvdm.academia.repositorio.MatriculaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest
class AcademiaApplicationTests {

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Test
    void testAlumnos() {
        alumnoRepository.findAll().forEach(System.out::println);
    }

    @Test
    void testCursos() {
        cursoRepository.findAll().forEach(System.out::println);
    }

    @Test
    void testMatriculas() {
        matriculaRepository.findAll().forEach(System.out::println);
    }

    /**
     *  1. Devuelve un listado de todos los cursos que se realizaron con fecha de inicio y fin durante el año 2025,
     *   cuya precio base sea superior a 500€.
     */
    @Test
    void test1() {
        List<Curso> cursosFiltrados = cursoRepository.findAll().stream()
                .filter(c -> c.getFechaInicio().getYear() == 2025 && c.getFechaFin().getYear() == 2025)
                .filter(c -> c.getPrecioBase() > 500)
                .toList();

        cursosFiltrados.forEach(System.out::println);
        Assertions.assertTrue(cursosFiltrados.stream().allMatch(c -> c.getPrecioBase() > 500));
    }

    /**
     * 2. Devuelve un listado de todos los alumnos que NO se han matriculado en ningún curso.
     */
    @Test
    void test2() {
        var sinMatricula = alumnoRepository.findAll().stream()
                .filter(a -> a.getMatriculas() == null || a.getMatriculas().isEmpty())
                .toList();

        sinMatricula.forEach(System.out::println);
        Assertions.assertTrue(sinMatricula.stream().allMatch(a -> a.getMatriculas() == null || a.getMatriculas().isEmpty()));
    }

    /**
     * 3. Devuelve una lista de los id's, nombres y emails de los alumnos que no tienen el teléfono registrado.
     * El listado tiene que estar ordenado inverso alfabéticamente por nombre (z..a)
     */
    @Test
    void test3() {
        var sinTelefono = alumnoRepository.findAll().stream()
                .filter(a -> a.getTelefono() == null || a.getTelefono().isEmpty())
                .sorted(Comparator.comparing(Alumno::getNombre).reversed())
                .toList();

        sinTelefono.forEach(a -> System.out.println(a.getId() + " - " + a.getNombre() + " - " + a.getEmail()));
        Assertions.assertTrue(sinTelefono.stream().allMatch(a -> a.getTelefono() == null || a.getTelefono().isEmpty()));
    }

    /**
     * 4. Devuelva un listado con los id's y emails de los alumnos que se hayan registrado con una cuenta de yahoo.es
     * en el año 2024.
     */
    @Test
    void test4() {
        var resultado = alumnoRepository.findAll().stream()
                .filter(a -> a.getEmail() != null && a.getEmail().endsWith("@yahoo.es"))
                .filter(a -> a.getFechaAlta().getYear() == 2024)
                .toList();

        resultado.forEach(a -> System.out.println(a.getId() + " - " + a.getEmail()));
        Assertions.assertTrue(resultado.stream().allMatch(a -> a.getEmail().endsWith("@yahoo.es")));
    }

    /**
     * 5. Devuelva un listado de los alumnos cuyo primer apellido es Martín.
     * Ordenado por fecha de alta (reciente a antigua), nombre y apellidos alfabéticamente.
     */
    @Test
    void test5() {
        var resultado = alumnoRepository.findAll().stream()
                .filter(a -> "Martín".equalsIgnoreCase(a.getApellido1()))
                .sorted(Comparator.comparing(Alumno::getFechaAlta).reversed()
                        .thenComparing(Alumno::getNombre)
                        .thenComparing(Alumno::getApellido1))
                .toList();

        resultado.forEach(System.out::println);
        Assertions.assertTrue(resultado.stream().allMatch(a -> "Martín".equalsIgnoreCase(a.getApellido1())));
    }

    /**
     * 6. Devuelva gasto total (pagado) que ha realizado la alumna Claudia López Rodríguez en cursos en la academia.
     */
    @Test
    void test6() {
        double total = matriculaRepository.findAll().stream()
                .filter(m -> {
                    Alumno a = m.getAlumno();
                    return a != null && "Claudia".equalsIgnoreCase(a.getNombre())
                            && "López".equalsIgnoreCase(a.getApellido1())
                            && "Rodríguez".equalsIgnoreCase(a.getApellido2());
                })
                .mapToDouble(Matricula::getImportePagado)
                .sum();

        System.out.println("Total gasto Claudia López Rodríguez: " + total);
        Assertions.assertTrue(total >= 0);
    }

    /**
     * 7. Devuelva el listado de los 3 cursos de menor importe base.
     */
    @Test
    void test7() {
        var resultado = cursoRepository.findAll().stream()
                .sorted(Comparator.comparing(Curso::getPrecioBase))
                .limit(3)
                .toList();

        resultado.forEach(System.out::println);
        Assertions.assertTrue(resultado.size() <= 3);
    }

    /**
     * 8. Devuelva el curso al que se le ha aplicado el mayor descuento en cuantía sobre su precio base.
     */
    @Test
    void test8() {
        var maxMatricula = matriculaRepository.findAll().stream()
                .max(Comparator.comparingDouble(m -> m.getCurso().getPrecioBase() - m.getImportePagado()));

        maxMatricula.ifPresent(m -> {
            System.out.println("Mayor descuento: " + (m.getCurso().getPrecioBase() - m.getImportePagado())
                    + " en curso: " + m.getCurso().getNombre());
        });

        Assertions.assertTrue(maxMatricula.isPresent());
    }

    /**
     * 9. Devuelve los alumnos que hayan obtenido un 10 como nota final en algún curso del que se han matriculado.
     */
    @Test
    void test9() {
        var alumnos10 = matriculaRepository.findAll().stream()
                .filter(m -> m.getNotaFinal() == 10)
                .map(Matricula::getAlumno)
                .distinct()
                .toList();

        alumnos10.forEach(System.out::println);
        Assertions.assertTrue(alumnos10.stream().allMatch(Objects::nonNull));
    }

    /**
     * 10. Devuelva el valor de la mínima nota obtenida en un curso.
     */
    @Test
    void test10() {
        var minNota = matriculaRepository.findAll().stream()
                .mapToDouble(Matricula::getNotaFinal)
                .min()
                .orElse(0);

        System.out.println("Nota mínima: " + minNota);
        Assertions.assertTrue(minNota >= 0);
    }

    /**
     * 11. Devuelve un listado de los cursos que empiecen por A y terminen por t, y también los cursos que terminen por x.
     */
    @Test
    void test11() {
        var resultado = cursoRepository.findAll().stream()
                .filter(c -> c.getNombre().matches("(?i)^A.*t$") || c.getNombre().toLowerCase().endsWith("x"))
                .toList();

        resultado.forEach(System.out::println);
    }

    /**
     * 12. Devuelve un listado que muestre todos los cursos en los que se ha matriculado cada alumno.
     */
    @Test
    void test12() {
        alumnoRepository.findAll().stream()
                .sorted(Comparator.comparing(Alumno::getNombre))
                .forEach(a -> {
                    System.out.println(a);
                    a.getMatriculas().forEach(m -> System.out.println("   -> " + m.getCurso().getNombre()));
                });
    }

    /**
     * 13. Devuelve el total de alumnos que podrían matricularse en la academia en base al cupo de los cursos.
     */
    @Test
    void test13() {
        int total = cursoRepository.findAll().stream()
                .mapToInt(Curso::getCupo)
                .sum();

        System.out.println("Total posible alumnos: " + total);
        Assertions.assertTrue(total >= 0);
    }

    /**
     * 14. Calcula el número total de alumnos (diferentes) que tienen alguna matrícula.
     */
    @Test
    void test14() {
        long total = matriculaRepository.findAll().stream()
                .map(Matricula::getAlumno)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        System.out.println("Total alumnos con matrícula: " + total);
        Assertions.assertTrue(total >= 0);
    }

    /**
     * 15. Devuelve el listado de cursos a los que se aplica un descuento porcentual (descuento_pct) superior al 10%.
     */
    @Test
    void test15() {
        var resultado = matriculaRepository.findAll().stream()
                .filter(m -> m.getDescuentoPct() != null && m.getDescuentoPct() > 10)
                .map(Matricula::getCurso)
                .distinct()
                .toList();

        resultado.forEach(System.out::println);
    }

    /**
     * 16. Devuelve el nombre del alumno que pagó la matrícula de mayor cuantía.
     */
    @Test
    void test16() {
        var maxPago = matriculaRepository.findAll().stream()
                .max(Comparator.comparingDouble(Matricula::getImportePagado));

        maxPago.ifPresent(m -> System.out.println("Alumno: " + m.getAlumno().getNombre() +
                " - Pago: " + m.getImportePagado()));
    }

    /**
     * 17. Devuelve los nombre de los alumnos que hayan sido compañeros en algún curso de la alumna Claudia López Rodríguez.
     */
    @Test
    void test17() {
        var matriculas = matriculaRepository.findAll();

        var cursosClaudia = matriculas.stream()
                .filter(m -> {
                    Alumno a = m.getAlumno();
                    return a != null && "Claudia".equalsIgnoreCase(a.getNombre())
                            && "López".equalsIgnoreCase(a.getApellido1())
                            && "Rodríguez".equalsIgnoreCase(a.getApellido2());
                })
                .map(Matricula::getCurso)
                .toList();

        var companeros = matriculas.stream()
                .filter(m -> cursosClaudia.contains(m.getCurso()))
                .map(Matricula::getAlumno)
                .filter(a -> !"Claudia".equalsIgnoreCase(a.getNombre()))
                .distinct()
                .toList();

        companeros.forEach(System.out::println);
    }

    /**
     * 18. Devuelve el total de lo ingresado por la academia en matriculas para el mes de enero de 2025.
     */
    @Test
    void test18() {
        double totalEnero = matriculaRepository.findAll().stream()
                .filter(m -> m.getFechaMatricula().getYear() == 2025 &&
                        m.getFechaMatricula().getMonth() == Month.JANUARY)
                .mapToDouble(Matricula::getImportePagado)
                .sum();

        System.out.println("Total ingresado en enero 2025: " + totalEnero);
    }

    /**
     * 19. Devuelve el conteo de cuantos alumnos tienen la observación de 'Requiere apoyo' en los cursos matriculados.
     */
    @Test
    void test19() {
        long total = matriculaRepository.findAll().stream()
                .filter(m -> "Requiere apoyo".equalsIgnoreCase(m.getObservaciones()))
                .map(Matricula::getAlumno)
                .distinct()
                .count();

        System.out.println("Total alumnos con 'Requiere apoyo': " + total);
    }

    /**
     * 20. Devuelve cuánto se ingresaría por el curso de 'Desarrollo Backend con Java' si todo el cupo estuviera matriculado.
     */
    @Test
    void test20() {
        cursoRepository.findAll().stream()
                .filter(c -> "Desarrollo Backend con Java".equalsIgnoreCase(c.getNombre()))
                .findFirst()
                .ifPresent(curso -> {
                    double total = curso.getCupo() * curso.getPrecioBase();
                    System.out.println("Ingresos potenciales: " + total);
                });
    }
}
