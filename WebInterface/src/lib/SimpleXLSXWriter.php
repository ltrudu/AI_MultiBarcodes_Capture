<?php

/**
 * Simple XLSX Writer
 * A lightweight PHP class for creating Excel XLSX files without external dependencies
 */
class SimpleXLSXWriter {
    private $data = [];
    private $filename;
    private $tempDir;

    public function __construct($filename = 'export.xlsx') {
        $this->filename = $filename;
        $this->tempDir = sys_get_temp_dir() . '/xlsx_' . uniqid();
        if (!is_dir($this->tempDir)) {
            mkdir($this->tempDir, 0777, true);
        }
    }

    public function addRow($row) {
        $this->data[] = $row;
    }

    public function writeToFile() {
        $this->createDirectoryStructure();
        $this->writeContentTypes();
        $this->writeRels();
        $this->writeApp();
        $this->writeCore();
        $this->writeWorkbook();
        $this->writeWorkbookRels();
        $this->writeWorksheet();
        $this->writeStyles();

        return $this->createZip();
    }

    private function createDirectoryStructure() {
        $dirs = [
            '_rels',
            'docProps',
            'xl',
            'xl/_rels',
            'xl/worksheets'
        ];

        foreach ($dirs as $dir) {
            $fullPath = $this->tempDir . '/' . $dir;
            if (!is_dir($fullPath)) {
                mkdir($fullPath, 0777, true);
            }
        }
    }

    private function writeContentTypes() {
        $content = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
<Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
<Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
</Types>';
        file_put_contents($this->tempDir . '/[Content_Types].xml', $content);
    }

    private function writeRels() {
        $content = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
<Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>';
        file_put_contents($this->tempDir . '/_rels/.rels', $content);
    }

    private function writeApp() {
        $content = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
<Application>Barcode WMS Export</Application>
<DocSecurity>0</DocSecurity>
<ScaleCrop>false</ScaleCrop>
<SharedDoc>false</SharedDoc>
<LinksUpToDate>false</LinksUpToDate>
<HyperlinksChanged>false</HyperlinksChanged>
<AppVersion>1.0</AppVersion>
</Properties>';
        file_put_contents($this->tempDir . '/docProps/app.xml', $content);
    }

    private function writeCore() {
        $now = date('Y-m-d\TH:i:s\Z');
        $content = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dcmitype="http://purl.org/dc/dcmitype/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<dc:creator>Barcode WMS</dc:creator>
<dcterms:created xsi:type="dcterms:W3CDTF">' . $now . '</dcterms:created>
<dcterms:modified xsi:type="dcterms:W3CDTF">' . $now . '</dcterms:modified>
<dc:title>Barcode Export</dc:title>
</cp:coreProperties>';
        file_put_contents($this->tempDir . '/docProps/core.xml', $content);
    }

    private function writeWorkbook() {
        $content = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets>
<sheet name="Barcode Export" sheetId="1" r:id="rId1"/>
</sheets>
</workbook>';
        file_put_contents($this->tempDir . '/xl/workbook.xml', $content);
    }

    private function writeWorkbookRels() {
        $content = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>';
        file_put_contents($this->tempDir . '/xl/_rels/workbook.xml.rels', $content);
    }

    private function writeWorksheet() {
        $content = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheetData>';

        $rowIndex = 1;
        foreach ($this->data as $row) {
            $content .= '<row r="' . $rowIndex . '">';
            $colIndex = 0;
            foreach ($row as $cell) {
                $cellRef = $this->columnIndexToLetter($colIndex) . $rowIndex;
                $content .= '<c r="' . $cellRef . '" t="inlineStr"><is><t>' . htmlspecialchars($cell) . '</t></is></c>';
                $colIndex++;
            }
            $content .= '</row>';
            $rowIndex++;
        }

        $content .= '</sheetData>
</worksheet>';
        file_put_contents($this->tempDir . '/xl/worksheets/sheet1.xml', $content);
    }

    private function writeStyles() {
        $content = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<fonts count="1">
<font>
<sz val="11"/>
<name val="Calibri"/>
</font>
</fonts>
<fills count="1">
<fill>
<patternFill patternType="none"/>
</fill>
</fills>
<borders count="1">
<border>
<left/>
<right/>
<top/>
<bottom/>
<diagonal/>
</border>
</borders>
<cellStyleXfs count="1">
<xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
</cellStyleXfs>
<cellXfs count="1">
<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
</cellXfs>
</styleSheet>';
        file_put_contents($this->tempDir . '/xl/styles.xml', $content);
    }

    private function columnIndexToLetter($index) {
        $letter = '';
        while ($index >= 0) {
            $letter = chr($index % 26 + 65) . $letter;
            $index = intval($index / 26) - 1;
        }
        return $letter;
    }

    private function createZip() {
        if (!class_exists('ZipArchive')) {
            throw new Exception('ZipArchive class not found. Please install php-zip extension.');
        }

        $zip = new ZipArchive();
        $zipFile = $this->tempDir . '.zip';

        if ($zip->open($zipFile, ZipArchive::CREATE) !== TRUE) {
            throw new Exception('Cannot create zip file: ' . $zipFile);
        }

        $this->addDirectoryToZip($zip, $this->tempDir, '');
        $zip->close();

        return $zipFile;
    }

    private function addDirectoryToZip($zip, $dir, $base) {
        $files = new RecursiveIteratorIterator(
            new RecursiveDirectoryIterator($dir),
            RecursiveIteratorIterator::LEAVES_ONLY
        );

        foreach ($files as $file) {
            if (!$file->isDir()) {
                $filePath = $file->getRealPath();
                $relativePath = $base . substr($filePath, strlen($dir) + 1);
                $relativePath = str_replace('\\', '/', $relativePath);
                $zip->addFile($filePath, $relativePath);
            }
        }
    }

    public function output() {
        $zipFile = $this->writeToFile();

        // Set headers for download
        header('Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
        header('Content-Disposition: attachment; filename="' . $this->filename . '"');
        header('Cache-Control: no-cache, must-revalidate');
        header('Expires: 0');
        header('Content-Length: ' . filesize($zipFile));

        // Output file
        readfile($zipFile);

        // Clean up
        unlink($zipFile);
        $this->deleteDirectory($this->tempDir);
    }

    private function deleteDirectory($dir) {
        if (!is_dir($dir)) return;

        $files = new RecursiveIteratorIterator(
            new RecursiveDirectoryIterator($dir, RecursiveDirectoryIterator::SKIP_DOTS),
            RecursiveIteratorIterator::CHILD_FIRST
        );

        foreach ($files as $file) {
            if ($file->isDir()) {
                rmdir($file->getRealPath());
            } else {
                unlink($file->getRealPath());
            }
        }
        rmdir($dir);
    }
}
?>