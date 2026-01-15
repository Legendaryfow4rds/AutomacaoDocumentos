package BancadaDeTestes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class InterfaceAutomacao extends JFrame {
    private JTextField txtPlanilha, txtPdfVig, txtPdfServ, txtDrive;
    private JTextArea areaLog;
    private JButton btnIniciar;

    public InterfaceAutomacao() {
        setTitle("Gocil - Extrator FGTS Digital (Painel Administrativo)");

        // --- AJUSTE DE DIMENSÃO (Mais largo e menos alto) ---
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(20, 20));
        getContentPane().setBackground(new Color(245, 245, 245));

        // --- PAINEL DE SELEÇÃO (Esticado para as laterais) ---
        JPanel painelInputs = new JPanel(new GridLayout(4, 1, 15, 15));
        painelInputs.setBorder(new EmptyBorder(30, 40, 10, 40)); // Margens laterais maiores
        painelInputs.setOpaque(false);

        txtPlanilha = criarCampoSelecao(painelInputs, "Planilha Excel:", false);
        txtPdfVig = criarCampoSelecao(painelInputs, "PDF Vigilância:", false);
        txtPdfServ = criarCampoSelecao(painelInputs, "PDF Serviço:", false);
        txtDrive = criarCampoSelecao(painelInputs, "Pasta do Drive:", true);

        // --- ÁREA DE RELATÓRIO (FUNDO BRANCO) ---
        areaLog = new JTextArea();
        areaLog.setBackground(Color.WHITE);
        areaLog.setForeground(Color.BLACK);
        areaLog.setFont(new Font("Consolas", Font.PLAIN, 14));
        areaLog.setEditable(false);
        areaLog.setMargin(new Insets(15, 15, 15, 15));

        JScrollPane scroll = new JScrollPane(areaLog);
        scroll.setBorder(BorderFactory.createTitledBorder("Relatório de Processamento em Tempo Real"));

        // --- PAINEL DO BOTÃO (Para centralizar o INICIAR com margens) ---
        JPanel painelBotao = new JPanel(new BorderLayout());
        painelBotao.setOpaque(false);
        painelBotao.setBorder(new EmptyBorder(0, 40, 30, 40));

        btnIniciar = new JButton("INICIAR");
        btnIniciar.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnIniciar.setPreferredSize(new Dimension(0, 65));
        btnIniciar.setBackground(Color.BLACK);
        btnIniciar.setForeground(Color.WHITE);
        btnIniciar.setOpaque(true);
        btnIniciar.setBorderPainted(false);
        btnIniciar.setFocusPainted(false);
        btnIniciar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnIniciar.addActionListener(e -> dispararProcesso());
        painelBotao.add(btnIniciar, BorderLayout.CENTER);

        // Adicionando ao Frame
        add(painelInputs, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(painelBotao, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    private JTextField criarCampoSelecao(JPanel pai, String rotulo, boolean apenasPasta) {
        JPanel linha = new JPanel(new BorderLayout(15, 0));
        linha.setOpaque(false);

        JLabel lbl = new JLabel(rotulo);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setPreferredSize(new Dimension(160, 25));

        JTextField campo = new JTextField();
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton btn = new JButton("Selecionar");
        btn.setPreferredSize(new Dimension(110, 25));

        btn.addActionListener(e -> {
            JFileChooser buscador = new JFileChooser();
            if (apenasPasta) buscador.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (buscador.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                campo.setText(buscador.getSelectedFile().getAbsolutePath());
            }
        });

        linha.add(lbl, BorderLayout.WEST);
        linha.add(campo, BorderLayout.CENTER);
        linha.add(btn, BorderLayout.EAST);
        pai.add(linha);
        return campo;
    }

    private void dispararProcesso() {
        if (txtPlanilha.getText().isEmpty() || txtDrive.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Atenção: Selecione os arquivos e o destino antes de iniciar.");
            return;
        }

        btnIniciar.setEnabled(false);
        btnIniciar.setText("PROCESSANDO...");
        areaLog.setText("🚀 [SISTEMA] Iniciando extração e sincronização...\n");

        new Thread(() -> {
            try {
                AutomacaoPrincipalV4.executarComParametros(
                        txtPlanilha.getText(),
                        txtPdfVig.getText(),
                        txtPdfServ.getText(),
                        txtDrive.getText(),
                        areaLog
                );
            } catch (Exception ex) {
                areaLog.append("\n❌ ERRO: " + ex.getMessage());
            } finally {
                btnIniciar.setEnabled(true);
                btnIniciar.setText("INICIAR");
            }
        }).start();
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new InterfaceAutomacao().setVisible(true));
    }
}
