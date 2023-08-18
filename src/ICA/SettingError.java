package ICA;
//Eccezione che viene sollevata nel momento in cui vengono rilevati errori nelle impostazioni iniziali
public class SettingError extends Exception {
	
	private static final long serialVersionUID = 7793190687685292908L;

	public SettingError(String s){
		super(s);
	}
}
