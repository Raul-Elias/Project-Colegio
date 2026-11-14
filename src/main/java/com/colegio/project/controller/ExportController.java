package com.colegio.project.controller;

import com.colegio.project.repository.AlumnoRepository;
import com.colegio.project.service.SheetsSyncService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.List;

/** Descarga de matriculados en Excel (.xlsx) lista para armar tablas dinámicas / dashboard. */
@RestController
public class ExportController {
    @Autowired private AlumnoRepository repo;

    @GetMapping("/api/alumnos/export.xlsx")
    public ResponseEntity<byte[]> exportar() throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("Matriculados");
            int r = 0;
            Row h = sh.createRow(r++);
            for (int i = 0; i < SheetsSyncService.CABECERA.size(); i++) h.createCell(i).setCellValue((String) SheetsSyncService.CABECERA.get(i));
            for (var a : repo.findAll()) {
                Row row = sh.createRow(r++);
                List<Object> f = SheetsSyncService.fila(a);
                for (int i = 0; i < f.size(); i++) {
                    Object v = f.get(i);
                    if (v instanceof Number nu) row.createCell(i).setCellValue(nu.doubleValue());
                    else row.createCell(i).setCellValue(v == null ? "" : v.toString());
                }
            }
            for (int i = 0; i < SheetsSyncService.CABECERA.size(); i++) sh.autoSizeColumn(i);
            wb.write(out);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=matriculados.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(out.toByteArray());
        }
    }
}
