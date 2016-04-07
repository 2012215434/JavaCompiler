import java.awt.BufferCapabilities;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

@SuppressWarnings("serial")
public class FileWindow extends JFrame implements ActionListener, Runnable {

	Thread compiler = null;
	Thread run_prom = null;
	boolean bn = true;
	CardLayout mycard;
	File file_saved = null;
	JButton button_input_text, button_compiler_text, button_compiler, button_run_prom, button_see_doswin;

	JPanel p = new JPanel();
	RSyntaxTextArea input_text = new RSyntaxTextArea();// program input area
	JTextArea compiler_text = new JTextArea();// compiler error display area
	JTextArea dos_out_text = new JTextArea();// running result of the program

	JTextField input_file_name_text = new JTextField();
	JTextField run_file_name_text = new JTextField();

	public FileWindow() {
		super("Java Compiler");
		mycard = new CardLayout();
		compiler = new Thread(this);
		run_prom = new Thread(this);
		button_input_text = new JButton("program input area(white)");
		button_compiler_text = new JButton("compiler result area(pink)");
		button_see_doswin = new JButton("the console(light blue)");
		button_compiler = new JButton("compile program");
		button_run_prom = new JButton("runnig program");

		p.setLayout(mycard);
		input_text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		input_text.setCodeFoldingEnabled(true);
		RTextScrollPane sp = new RTextScrollPane(input_text);

		p.add("input", sp);
		p.add("compiler", compiler_text);
		p.add("dos", dos_out_text);
		add(p, "Center");

		CompletionProvider provider = createCompletionProvider();
		AutoCompletion ac = new AutoCompletion(provider);
		ac.install(input_text);

		compiler_text.setBackground(Color.pink);
		dos_out_text.setBackground(Color.cyan);
		JPanel p1 = new JPanel();

		p1.setLayout(new GridLayout(3, 3));

		p1.add(button_input_text);
		p1.add(button_compiler_text);
		p1.add(button_see_doswin);
		p1.add(new JLabel("input compile file name(.java):"));
		p1.add(input_file_name_text);
		p1.add(button_compiler);
		p1.add(new JLabel("input application main class name"));
		p1.add(run_file_name_text);
		p1.add(button_run_prom);
		add(p1, "North");

		// define events
		button_input_text.addActionListener(this);
		button_compiler.addActionListener(this);
		button_compiler_text.addActionListener(this);
		button_run_prom.addActionListener(this);
		button_see_doswin.addActionListener(this);

	}
	
	private CompletionProvider createCompletionProvider() {

	      // A DefaultCompletionProvider is the simplest concrete implementation
	      // of CompletionProvider. This provider has no understanding of
	      // language semantics. It simply checks the text entered up to the
	      // caret position for a match against known completions. This is all
	      // that is needed in the majority of cases.
	      DefaultCompletionProvider provider = new DefaultCompletionProvider();

	      // Add completions for all Java keywords. A BasicCompletion is just
	      // a straightforward word completion.
	      provider.addCompletion(new BasicCompletion(provider, "abstract"));
	      provider.addCompletion(new BasicCompletion(provider, "assert"));
	      provider.addCompletion(new BasicCompletion(provider, "break"));
	      provider.addCompletion(new BasicCompletion(provider, "case"));
	      // ... etc ...
	      provider.addCompletion(new BasicCompletion(provider, "transient"));
	      provider.addCompletion(new BasicCompletion(provider, "try"));
	      provider.addCompletion(new BasicCompletion(provider, "void"));
	      provider.addCompletion(new BasicCompletion(provider, "volatile"));
	      provider.addCompletion(new BasicCompletion(provider, "while"));

	      // Add a couple of "shorthand" completions. These completions don't
	      // require the input text to be the same thing as the replacement text.
	      provider.addCompletion(new ShorthandCompletion(provider, "sysout",
	            "System.out.println(", "System.out.println("));
	      provider.addCompletion(new ShorthandCompletion(provider, "syso",
		            "System.out.println(", "System.out.println("));
	      provider.addCompletion(new ShorthandCompletion(provider, "syserr",
	            "System.err.println(", "System.err.println("));
	      

	      return provider;

	   }

	@Override
	public void run() {
		if (Thread.currentThread() == compiler) {
			compiler_text.setText(null);
			String temp = input_text.getText().trim();
			byte[] buffer = temp.getBytes();
			int b = buffer.length;
			String file_name = null;
			file_name = input_file_name_text.getText().trim();
			try {

				file_saved = new File(file_name);
				FileOutputStream writefile = null;
				writefile = new FileOutputStream(file_saved);
				writefile.write(buffer, 0, b);
				writefile.close();

			} catch (Exception e) {
				System.out.println("ERROR");
			}

			try {
				Runtime rt = Runtime.getRuntime();
				InputStream in = rt.exec("javac " + file_name).getErrorStream();
				BufferedInputStream bufIn = new BufferedInputStream(in);
				byte[] buff = new byte[100];
				int n = 0;
				boolean flag = true;

				while ((n = bufIn.read(buff, 0, buff.length)) != -1) {
					String s = null;
					s = new String(buff, 0, n);
					compiler_text.append(s);
					if (s != null) {
						flag = false;
					}
				}
				if (flag) {
					compiler_text.append("compile succeed!");
				}
			} catch (Exception e) {

			}
		} else if (Thread.currentThread() == run_prom) {
			dos_out_text.setText(null);

			try {
				Runtime rt = Runtime.getRuntime();
				String path = run_file_name_text.getText().trim();
				Process stream = rt.exec("java " + path);
				InputStream in = stream.getInputStream();
				BufferedInputStream bisErr = new BufferedInputStream(stream.getErrorStream());
				BufferedInputStream bisIn = new BufferedInputStream(in);
				byte[] buf = new byte[150];
				byte[] err_buf = new byte[150];

				@SuppressWarnings("unused")
				int m = 0;
				@SuppressWarnings("unused")
				int i = 0;
				String s = null;
				String err = null;

				// 打印编译信息及错误信息
				while ((m = bisIn.read(buf, 0, 150)) != -1) {
					s = new String(buf, 0, 150);
					dos_out_text.append(s);
				}
				while ((i = bisErr.read(err_buf)) != -1) {
					err = new String(err_buf, 0, 150);
					dos_out_text.append(err);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == button_input_text) {
			mycard.show(p, "input");
		} else if (e.getSource() == button_compiler_text) {
			mycard.show(p, "compiler");
		} else if (e.getSource() == button_see_doswin) {
			mycard.show(p, "dos");
		} else if (e.getSource() == button_compiler) {
			if (!(compiler.isAlive())) {
				compiler = new Thread(this);
			}
			try {
				compiler.start();
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			mycard.show(p, "compiler");
		} else if (e.getSource() == button_run_prom) {
			if (!(run_prom.isAlive())) {
				run_prom = new Thread(this);
			}
			try {
				run_prom.start();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			mycard.show(p, "dos");
		}
	}

}
