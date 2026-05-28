package com.trabalho.tag.service;

import com.trabalho.tag.model.Certificado;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Responsável por montar e disparar o e-mail com o certificado em PDF.
 * O envio é assíncrono (@Async) para não bloquear a resposta HTTP.
 */
@Service
public class CertificadoEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private CertificadoPdfService certificadoPdfService;

    @Value("${muttley.mail.remetente}")
    private String remetente;

    /**
     * Gera o PDF e envia o certificado por e-mail ao participante.
     */
    @Async
    public void enviarCertificado(Certificado certificado) {

        if (certificado.getEventoParticipante() == null
                || certificado.getEventoParticipante().getParticipante() == null) {
            return;
        }

        String emailDestino = certificado.getEventoParticipante().getParticipante().getEmail();
        if (emailDestino == null || emailDestino.isBlank()) {
            return;
        }

        try {
            String nomeParticipante = certificado.getEventoParticipante().getParticipante().getNome();
            String nomeEvento = certificado.getEventoParticipante().getEvento() != null
                    ? certificado.getEventoParticipante().getEvento().getTitulo()
                    : "Evento";

            // Gera o PDF no modelo FATEC
            byte[] pdfBytes = certificadoPdfService.gerarPdf(certificado);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(remetente);
            helper.setTo(emailDestino);
            helper.setSubject("🎓 Seu certificado — " + nomeEvento);
            helper.setText(corpoEmail(nomeParticipante, nomeEvento), true);

            // Anexo: PDF do certificado
            final byte[] pdfFinal = pdfBytes;
            helper.addAttachment(
                    "certificado-" + certificado.getId() + ".pdf",
                    () -> new java.io.ByteArrayInputStream(pdfFinal),
                    "application/pdf"
            );

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("[CertificadoEmailService] Falha ao enviar e-mail para "
                    + emailDestino + ": " + e.getMessage());
        }
    }

    // ── Corpo HTML do e-mail ──────────────────────────────────────────────────

    private String corpoEmail(String nome, String nomeEvento) {
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head><meta charset="UTF-8"></head>
                <body style="font-family:sans-serif;background:#f5f5f5;padding:2rem;">
                  <div style="max-width:560px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;
                              box-shadow:0 4px 20px rgba(0,0,0,.1);">
                    <div style="background:#1a1a2e;padding:1.5rem 2rem;">
                      <p style="color:#F5C518;font-size:.8rem;letter-spacing:.15em;text-transform:uppercase;margin:0 0 4px;">
                        Muttley — Gestão Acadêmica
                      </p>
                      <h1 style="color:#fff;font-size:1.3rem;margin:0;">🎓 Seu certificado chegou!</h1>
                    </div>
                    <div style="padding:1.75rem 2rem;">
                      <p style="color:#333;line-height:1.6;">Olá, <strong>%s</strong>!</p>
                      <p style="color:#555;line-height:1.6;margin-top:.75rem;">
                        Sua participação no evento <strong>%s</strong> foi confirmada e seu
                        certificado foi gerado automaticamente.
                      </p>
                      <p style="color:#555;line-height:1.6;margin-top:.75rem;">
                        O certificado em PDF está anexado a este e-mail. Salve-o para seus registros.
                      </p>
                    </div>
                    <div style="background:#f9f9f9;padding:1rem 2rem;border-top:1px solid #eee;">
                      <p style="color:#aaa;font-size:.78rem;margin:0;">
                        Este é um e-mail automático. Não responda.
                      </p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(nome, nomeEvento);
    }
}
