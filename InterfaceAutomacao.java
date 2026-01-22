package BancadaDeTestes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class InterfaceAutomacao extends JFrame {
    private JTextField txtPlanilha, txtPdfVig, txtPdfServ, txtDrive;
    private JTextArea areaLog;
    private JButton btnIniciar;

    public InterfaceAutomacao() {
        setTitle("Gocil - Extrator FGTS Digital (Painel Administrativo)");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));

        // Fundo Totalmente Branco
        getContentPane().setBackground(Color.WHITE);

        // --- PAINEL SUPERIOR (SELEÇÃO) ---
        JPanel painelInputs = new JPanel(new GridLayout(4, 1, 12, 12));
        painelInputs.setBackground(Color.WHITE);
        painelInputs.setBorder(new EmptyBorder(30, 50, 20, 50));

        txtPlanilha = criarCampoEstiloWindows(painelInputs, "Planilha Excel:", false);
        txtPdfVig = criarCampoEstiloWindows(painelInputs, "PDF Vigilância:", false);
        txtPdfServ = criarCampoEstiloWindows(painelInputs, "PDF Serviço:", false);
        txtDrive = criarCampoEstiloWindows(painelInputs, "Pasta do Drive:", true);

        // --- PAINEL CENTRAL (LOG) ---
        areaLog = new JTextArea();
        areaLog.setBackground(new Color(252, 252, 252)); // Quase branco para o log
        areaLog.setForeground(Color.BLACK);
        areaLog.setFont(new Font("Consolas", Font.PLAIN, 13));
        areaLog.setEditable(false);

        JScrollPane scroll = new JScrollPane(areaLog);
        // Borda com título elegante
        TitledBorder bordaLog = BorderFactory.createTitledBorder(
                new LineBorder(new Color(0, 0, 0), 2), " Status do Processamento ");
        bordaLog.setTitleFont(new Font("Segoe UI", Font.BOLD, 12));
        scroll.setBorder(bordaLog);

        JPanel painelCentral = new JPanel(new BorderLayout());
        painelCentral.setBackground(Color.WHITE);
        painelCentral.setBorder(new EmptyBorder(0, 50, 10, 50));
        painelCentral.add(scroll, BorderLayout.CENTER);

        // --- PAINEL INFERIOR (BOTÃO VERDE) ---
        JPanel painelBotao = new JPanel(new FlowLayout(FlowLayout.CENTER));
        painelBotao.setBackground(Color.WHITE);
        painelBotao.setBorder(new EmptyBorder(10, 0, 35, 0));

        btnIniciar = new JButton("INICIAR PROCESSO");
        btnIniciar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnIniciar.setPreferredSize(new Dimension(280, 50));
        btnIniciar.setBackground(new Color(39, 174, 96)); // Verde Esmeralda
        btnIniciar.setForeground(Color.WHITE);
        btnIniciar.setFocusPainted(false);
        btnIniciar.setBorderPainted(false);
        btnIniciar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnIniciar.addActionListener(e -> dispararProcesso());
        painelBotao.add(btnIniciar);

        add(painelInputs, BorderLayout.NORTH);
        add(painelCentral, BorderLayout.CENTER);
        add(painelBotao, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    private JTextField criarCampoEstiloWindows(JPanel pai, String rotulo, boolean apenasPasta) {
        JPanel linha = new JPanel(new BorderLayout(15, 0));
        linha.setOpaque(false);

        JLabel lbl = new JLabel(rotulo);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setPreferredSize(new Dimension(130, 25));

        JTextField campo = new JTextField();
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        campo.setBackground(new Color(248, 249, 250)); // Cinza ultra claro (estilo Explorer)
        campo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(5, 10, 5, 10)
        ));

        JButton btn = new JButton("Procurar");
        btn.setFocusPainted(false);
        btn.setBackground(new Color(240, 240, 240));

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
            JOptionPane.showMessageDialog(this, "Por favor, selecione os caminhos.");
            return;
        }

        btnIniciar.setEnabled(false);
        btnIniciar.setText("PROCESSANDO...");
        btnIniciar.setBackground(Color.LIGHT_GRAY);
        areaLog.setText("⌛ Aguardando inicialização dos motores...\n");

        new Thread(() -> {
            try {
                AutomacaoPrincipalV4.executarComParametros(
                        txtPlanilha.getText(), txtPdfVig.getText(), txtPdfServ.getText(), txtDrive.getText(), areaLog
                );
            } catch (Exception ex) {
                areaLog.append("\n❌ ERRO CRÍTICO: " + ex.getMessage());
            } finally {
                SwingUtilities.invokeLater(() -> {
                    btnIniciar.setEnabled(true);
                    btnIniciar.setText("INICIAR PROCESSO");
                    btnIniciar.setBackground(new Color(39, 174, 96));
                });
            }
        }).start();
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new InterfaceAutomacao().setVisible(true));
    }
}
