package com.trabalho.tag.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.trabalho.tag.model.Certificado;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Gera o PDF do certificado no modelo visual da FATEC Zona Leste:
 * fundo cinza claro, título amarelo em caps, nome do aluno em negrito,
 * texto descritivo e logos institucionais simulados.
 */
@Service
public class CertificadoPdfService {

    // Cores do modelo FATEC
    private static final DeviceRgb AMARELO      = new DeviceRgb(0xF5, 0xC5, 0x18); // #F5C518
    private static final DeviceRgb CINZA_FUNDO  = new DeviceRgb(0xE8, 0xE8, 0xE8); // #E8E8E8
    private static final DeviceRgb VERMELHO_SP  = new DeviceRgb(0xCC, 0x00, 0x00); // #CC0000
    private static final DeviceRgb CINZA_TEXTO  = new DeviceRgb(0x33, 0x33, 0x33); // #333333

    /**
     * Gera o certificado em PDF e retorna os bytes.
     */
    public byte[] gerarPdf(Certificado cert) throws Exception {

        // ── Dados do certificado ──────────────────────────────────────────────
        String nomeAluno    = cert.getEventoParticipante().getParticipante().getNome();
        String nomeEvento   = cert.getEventoParticipante().getEvento().getTitulo();
        String tipoEvento   = cert.getEventoParticipante().getEvento().getTipoEvento() != null
                ? cert.getEventoParticipante().getEvento().getTipoEvento().name()
                : "Evento";

        // Carga horária formatada: "2h30" ou "150 minutos"
        Integer chMin = cert.getEventoParticipante().getEvento().getCargaHorariaMinutos();
        String cargaHoraria = "";
        if (chMin != null && chMin > 0) {
            long h = chMin / 60, m = chMin % 60;
            cargaHoraria = h > 0
                    ? h + "h" + (m > 0 ? String.format("%02d", m) : "")
                    : m + " minutos";
        }

        // Data do evento
        String dataEvento = "";
        if (cert.getEventoParticipante().getEvento().getDataInicio() != null) {
            dataEvento = cert.getEventoParticipante().getEvento().getDataInicio()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }

        // Data de emissão (hoje)
        String dataEmissao = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        // ── Criação do PDF ────────────────────────────────────────────────────
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Orientação paisagem (A4 rotado)
        PageSize pageSize = PageSize.A4.rotate();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        pdfDoc.addNewPage(pageSize);

        PdfPage  page   = pdfDoc.getFirstPage();
        PdfCanvas canvas = new PdfCanvas(page);

        float W = pageSize.getWidth();   // ~841
        float H = pageSize.getHeight();  // ~595

        // ── Fundo cinza ───────────────────────────────────────────────────────
        canvas.setFillColor(CINZA_FUNDO)
              .rectangle(0, 0, W, H)
              .fill();

        // ── Decorações de canto (triângulos: amarelo e vermelho) ──────────────
        // Canto superior-esquerdo (amarelo + vermelho)
        desenharCantoSuperiorEsquerdo(canvas);
        // Canto inferior-direito (amarelo + vermelho)
        desenharCantoInferiorDireito(canvas, W, H);
        // Canto superior-direito (vermelho)
        desenharCantoSuperiorDireito(canvas, W, H);
        // Canto inferior-esquerdo (amarelo)
        desenharCantoInferiorEsquerdo(canvas, H);

        // ── Fontes ────────────────────────────────────────────────────────────
        PdfFont fonteBold  = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont fonteNormal= PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // ── Canvas de layout (para Paragraph) ────────────────────────────────
        Canvas layoutCanvas = new Canvas(canvas, new Rectangle(0, 0, W, H));

        // ── TÍTULO "C E R T I F I C A D O" ───────────────────────────────────
        layoutCanvas.add(
            new Paragraph("C E R T I F I C A D O")
                .setFont(fonteBold)
                .setFontSize(38)
                .setFontColor(AMARELO)
                .setTextAlignment(TextAlignment.CENTER)
                .setFixedPosition(0, H - 110, W)
        );

        // ── Linha "Este certificado é concedido ao Aluno" ─────────────────────
        layoutCanvas.add(
            new Paragraph("Este certificado é concedido ao Aluno")
                .setFont(fonteNormal)
                .setFontSize(14)
                .setFontColor(CINZA_TEXTO)
                .setTextAlignment(TextAlignment.CENTER)
                .setFixedPosition(0, H - 165, W)
        );

        // ── Nome do aluno ─────────────────────────────────────────────────────
        layoutCanvas.add(
            new Paragraph(nomeAluno)
                .setFont(fonteBold)
                .setFontSize(22)
                .setFontColor(CINZA_TEXTO)
                .setTextAlignment(TextAlignment.CENTER)
                .setFixedPosition(0, H - 205, W)
        );

        // ── Texto descritivo do evento ────────────────────────────────────────
        String textoEvento = String.format(
            "Por participar do evento %s %s realizado no dia %s,\npromovido pela FATEC Zona Leste.",
            tipoEvento.equals("WORKSHOP") ? "Workshop de" : tipoEvento + " —",
            nomeEvento, dataEvento
        );
        layoutCanvas.add(
            new Paragraph(textoEvento)
                .setFont(fonteNormal)
                .setFontSize(13)
                .setFontColor(CINZA_TEXTO)
                .setTextAlignment(TextAlignment.JUSTIFIED)
                .setFixedPosition(80, H - 295, W - 160)
        );

        // ── Carga horária ─────────────────────────────────────────────────────
        if (!cargaHoraria.isEmpty()) {
            layoutCanvas.add(
                new Paragraph("O evento foi realizado com carga horária de " + cargaHoraria + ".")
                    .setFont(fonteNormal)
                    .setFontSize(13)
                    .setFontColor(CINZA_TEXTO)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFixedPosition(80, H - 340, W - 160)
            );
        }

        // ── Data de emissão ───────────────────────────────────────────────────
        layoutCanvas.add(
            new Paragraph("São Paulo, " + dataEmissao)
                .setFont(fonteNormal)
                .setFontSize(13)
                .setFontColor(CINZA_TEXTO)
                .setTextAlignment(TextAlignment.CENTER)
                .setFixedPosition(0, H - 390, W)
        );

        // ── Linha de assinatura ───────────────────────────────────────────────
        float linhaX1 = W / 2 - 80;
        float linhaX2 = W / 2 + 80;
        float linhaY  = H - 460;
        canvas.setStrokeColor(CINZA_TEXTO)
              .setLineWidth(1f)
              .moveTo(linhaX1, linhaY)
              .lineTo(linhaX2, linhaY)
              .stroke();

        layoutCanvas.add(
            new Paragraph("Coordenador do curso de Análise e Desenvolvimento de\nSistemas da FATEC Zona Leste")
                .setFont(fonteNormal)
                .setFontSize(10)
                .setFontColor(CINZA_TEXTO)
                .setTextAlignment(TextAlignment.CENTER)
                .setFixedPosition(0, H - 490, W)
        );

        // ── Rodapé: logos textuais ────────────────────────────────────────────
        layoutCanvas.add(
            new Paragraph("Fatec Zona Leste")
                .setFont(fonteBold)
                .setFontSize(13)
                .setFontColor(CINZA_TEXTO)
                .setTextAlignment(TextAlignment.LEFT)
                .setFixedPosition(80, 28, 200)
        );

        layoutCanvas.add(
            new Paragraph("55 anos CPS")
                .setFont(fonteBold)
                .setFontSize(11)
                .setFontColor(CINZA_TEXTO)
                .setTextAlignment(TextAlignment.CENTER)
                .setFixedPosition(W / 2 - 60, 28, 120)
        );

        layoutCanvas.add(
            new Paragraph("SÃO PAULO\nGOVERNO DO ESTADO")
                .setFont(fonteBold)
                .setFontSize(9)
                .setFontColor(CINZA_TEXTO)
                .setTextAlignment(TextAlignment.RIGHT)
                .setFixedPosition(W - 200, 24, 120)
        );

        layoutCanvas.close();
        pdfDoc.close();

        return baos.toByteArray();
    }

    // ── Helpers de decoração de cantos ────────────────────────────────────────

    private void desenharCantoSuperiorEsquerdo(PdfCanvas c) {
        // Triângulo amarelo maior
        c.setFillColor(AMARELO)
         .moveTo(0, 595).lineTo(0, 480).lineTo(70, 595).closePathFillStroke();
        // Triângulo vermelho menor sobreposto
        c.setFillColor(VERMELHO_SP)
         .moveTo(0, 595).lineTo(0, 540).lineTo(40, 595).closePathFillStroke();
    }

    private void desenharCantoInferiorDireito(PdfCanvas c, float W, float H) {
        c.setFillColor(AMARELO)
         .moveTo(W, 0).lineTo(W - 70, 0).lineTo(W, 115).closePathFillStroke();
        c.setFillColor(VERMELHO_SP)
         .moveTo(W, 0).lineTo(W - 40, 0).lineTo(W, 60).closePathFillStroke();
    }

    private void desenharCantoSuperiorDireito(PdfCanvas c, float W, float H) {
        // Pequeno triângulo vermelho no canto superior-direito
        c.setFillColor(VERMELHO_SP)
         .moveTo(W, H).lineTo(W - 60, H).lineTo(W, H - 60).closePathFillStroke();
        c.setFillColor(AMARELO)
         .moveTo(W, H).lineTo(W - 30, H).lineTo(W, H - 30).closePathFillStroke();
    }

    private void desenharCantoInferiorEsquerdo(PdfCanvas c, float H) {
        // Pequeno triângulo amarelo inferior-esquerdo
        c.setFillColor(AMARELO)
         .moveTo(0, 0).lineTo(60, 0).lineTo(0, 60).closePathFillStroke();
        c.setFillColor(VERMELHO_SP)
         .moveTo(0, 0).lineTo(30, 0).lineTo(0, 30).closePathFillStroke();
    }
}
