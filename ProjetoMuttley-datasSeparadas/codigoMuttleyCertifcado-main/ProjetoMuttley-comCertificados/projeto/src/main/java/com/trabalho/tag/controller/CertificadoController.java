package com.trabalho.tag.controller;

import com.trabalho.tag.model.Certificado;
import com.trabalho.tag.repository.CertificadoRepository;
import com.trabalho.tag.service.CertificadoPdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/certificados")
public class CertificadoController {

    @Autowired
    private CertificadoRepository certificadoRepository;

    @Autowired
    private CertificadoPdfService certificadoPdfService;

    /**
     * Baixa o certificado como PDF (modelo FATEC).
     * GET /certificados/{certificadoId}/pdf
     */
    @GetMapping("/{certificadoId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long certificadoId) {

        Certificado cert = certificadoRepository.findById(certificadoId).orElse(null);
        if (cert == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] pdfBytes = certificadoPdfService.gerarPdf(cert);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"certificado-" + certificadoId + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lista os certificados de um participante (JSON — usado pelo dashboard).
     * GET /certificados/participante/{participanteId}
     */
    @GetMapping("/participante/{participanteId}")
    @ResponseBody
    public ResponseEntity<List<Certificado>> listarPorParticipante(@PathVariable Long participanteId) {
        List<Certificado> lista = certificadoRepository
                .findByEventoParticipante_Participante_Id(participanteId);
        return ResponseEntity.ok(lista);
    }

    /**
     * Emite/baixa o certificado como HTML imprimível.
     * GET /certificados/{certificadoId}/emitir
     */
    @GetMapping("/{certificadoId}/emitir")
    public ResponseEntity<byte[]> emitirCertificado(@PathVariable Long certificadoId) {

        Certificado cert = certificadoRepository.findById(certificadoId)
                .orElse(null);

        if (cert == null) {
            return ResponseEntity.notFound().build();
        }

        // Puxa o tipo real do banco (Especial, Comum, etc.) para o título do documento
        String tipoCertificado = cert.getTipo() != null ? "Certificado " + cert.getTipo() : "Certificado de Participação";

        String nomeParticipante = cert.getEventoParticipante() != null
                && cert.getEventoParticipante().getParticipante() != null
                ? cert.getEventoParticipante().getParticipante().getNome()
                : "Participante";

        String nomeEvento = cert.getEventoParticipante() != null
                && cert.getEventoParticipante().getEvento() != null
                ? cert.getEventoParticipante().getEvento().getTitulo()
                : "Evento";

        String dataEvento = "";
        Integer cargaHoraria = null;
        if (cert.getEventoParticipante() != null && cert.getEventoParticipante().getEvento() != null) {
            var evento = cert.getEventoParticipante().getEvento();
            if (evento.getDataInicio() != null) {
                dataEvento = evento.getDataInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            cargaHoraria = evento.getCargaHorariaMinutos();
        }

        String cargaHorariaTexto = "";
        if (cargaHoraria != null && cargaHoraria > 0) {
            long horas = cargaHoraria / 60;
            long minutos = cargaHoraria % 60;
            cargaHorariaTexto = horas > 0
                    ? horas + "h" + (minutos > 0 ? String.format("%02d", minutos) + "min" : "")
                    : minutos + " minutos";
        }

        // Passando as variáveis corrigidas na ordem exata dos marcadores
        String html = gerarHtmlCertificado(tipoCertificado, nomeParticipante, nomeEvento, dataEvento, cargaHorariaTexto, cert.getId());
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"certificado-" + certificadoId + ".html\"")
                .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
                .body(bytes);
    }

    // ── Geração do HTML do certificado corrigida ───────────────────────────────

    private String gerarHtmlCertificado(String tipoCertificado, String nomeParticipante, String nomeEvento,
                                         String data, String cargaHoraria, Long certId) {
        return """
<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="UTF-8">
<title>%s — %s</title>
<style>
  @import url('https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,700;1,400&family=Inter:wght@300;400;500&display=swap');
  *, *::before, *::after { margin: 0; padding: 0; box-sizing: border-box; }
  body { background: #f0ebe3; display: flex; align-items: center; justify-content: center; min-height: 100vh; padding: 2rem; }
  .cert {
    width: 900px; background: #fff;
    border: 12px solid #1a1a2e; padding: 60px 70px;
    text-align: center; position: relative;
    box-shadow: 0 20px 60px rgba(0,0,0,.25);
  }
  .cert::before {
    content: ''; position: absolute; inset: 10px;
    border: 2px solid #b8962e; pointer-events: none;
  }
  .logo { font-size: 2.4rem; margin-bottom: 6px; }
  .inst { font-family: 'Inter', sans-serif; font-size: .78rem; letter-spacing: .2em; text-transform: uppercase; color: #888; margin-bottom: 28px; }
  .titulo { font-family: 'Playfair Display', serif; font-size: 2rem; font-weight: 700; color: #1a1a2e; letter-spacing: .05em; text-transform: uppercase; margin-bottom: 28px; }
  .verb { font-family: 'Inter', sans-serif; font-size: .9rem; color: #555; margin-bottom: 18px; }
  .nome { font-family: 'Playfair Display', serif; font-style: italic; font-size: 2.8rem; color: #1a1a2e; border-bottom: 2px solid #b8962e; display: inline-block; padding-bottom: 6px; margin-bottom: 24px; }
  .evento-label { font-family: 'Inter', sans-serif; font-size: .82rem; text-transform: uppercase; letter-spacing: .15em; color: #888; margin-bottom: 8px; }
  .evento-nome { font-family: 'Playfair Display', serif; font-size: 1.5rem; color: #1a1a2e; margin-bottom: 28px; line-height: 1.35; }
  .meta { display: flex; justify-content: center; gap: 3rem; margin-bottom: 40px; }
  .meta-item { text-align: center; }
  .meta-label { font-family: 'Inter', sans-serif; font-size: .72rem; text-transform: uppercase; letter-spacing: .12em; color: #aaa; margin-bottom: 3px; }
  .meta-val { font-family: 'Inter', sans-serif; font-size: .9rem; font-weight: 500; color: #333; }
  .assinatura { display: flex; justify-content: center; gap: 5rem; margin-top: 10px; }
  .ass-item { text-align: center; }
  .ass-linha { width: 160px; border-bottom: 1.5px solid #333; margin: 0 auto 6px; }
  .ass-nome { font-family: 'Inter', sans-serif; font-size: .78rem; color: #444; }
  .ass-cargo { font-family: 'Inter', sans-serif; font-size: .7rem; color: #999; }
  .cert-id { position: absolute; bottom: 14px; right: 20px; font-family: 'Inter', sans-serif; font-size: .65rem; color: #ccc; }
  @media print {
    body { background: none; padding: 0; }
    .cert { box-shadow: none; width: 100%%; }
    .no-print { display: none; }
  }
</style>
</head>
<body>
<div class="cert">
  <div class="logo">🎓</div>
  <div class="inst">Sistema de Gestão Acadêmica — Muttley</div>
  <div class="titulo">%s</div>
  <p class="verb">Certificamos que</p>
  <div class="nome">%s</div>
  <div class="evento-label">participou do evento</div>
  <div class="evento-nome">%s</div>
  <div class="meta">
    %s
    %s
  </div>
  <div class="assinatura">
    <div class="ass-item">
      <div class="ass-linha"></div>
      <div class="ass-nome">Coordenação do Evento</div>
      <div class="ass-cargo">Organizador</div>
    </div>
    <div class="ass-item">
      <div class="ass-linha"></div>
      <div class="ass-nome">Direção Acadêmica</div>
      <div class="ass-cargo">Instituição</div>
    </div>
  </div>
  <div class="cert-id">Nº %d</div>
</div>
<div class="no-print" style="text-align:center;margin-top:1.5rem;">
  <button onclick="window.print()" style="padding:.6rem 1.4rem;background:#1a1a2e;color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:.9rem;">🖨️ Imprimir / Salvar PDF</button>
</div>
</body>
</html>
""".formatted(
                tipoCertificado,       // 1º %s -> Na tag <title>
                tipoCertificado,       // 2º %s -> Na tag <div class="titulo">
                nomeParticipante,      // 3º %s -> Na tag <div class="nome">
                nomeEvento,            // 4º %s -> Na tag <div class="evento-nome">
                data.isEmpty() ? "" : "<div class=\"meta-item\"><div class=\"meta-label\">Data</div><div class=\"meta-val\">" + data + "</div></div>", // 5º %s -> Meta Data
                cargaHoraria.isEmpty() ? "" : "<div class=\"meta-item\"><div class=\"meta-label\">Carga Horária</div><div class=\"meta-val\">" + cargaHoraria + "</div></div>", // 6º %s -> Meta Carga Horária
                certId                 // 7º %d -> Na tag <div class="cert-id">
        );
    }
}