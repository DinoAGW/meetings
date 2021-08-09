package utilities;

public class Drive_draft {
	private static final String fs = System.getProperty("file.separator");
	
	public static final String str = System.getProperty("user.home").concat(fs);
	
	public static class IeCreation {
		public static final String str = Drive_draft.str.concat(".meetings").concat(fs);
		
		public static class Abstract {
			public static final String str = IeCreation.str.concat("Abstracts").concat(fs);
		}
		
		public static class Meeting {
			public static final String str = IeCreation.str.concat("Ueberordnungen").concat(fs);
		}
	}
	
	public static class MetsSIPs {
		public static final String str = Drive_draft.str.concat("workspace").concat(fs).concat("metsSIPs").concat(fs);
		
		public static class preSIPs {
			public static final String str = MetsSIPs.str.concat("preSIPs").concat(fs);
		}
		
		public static class RosettaInstance {			
			private static class MaterialflowId {
				public static String str;
				
				private static class ProducerId {
					public static String str;
					
					public static class Abstract2 {
						public static String str;
						
						public static class Mets {
							public static final String str = Abstract2.str.concat("content").concat(fs).concat("ie1.xml");
						}
						
						public static Mets Mets () {
							Mets ret = new Mets();
							return ret;
						}
					}
					
					private static class Meeting {
						public static String str;
					}
					
					public static Abstract2 Abstract (String value) {
						Abstract2 ret = new Abstract2();
						ProducerId.Abstract2.str = str.concat(value).concat(fs);
						return ret;
					}
					
					public static Meeting Meeting (String value) {
						Meeting ret = new Meeting();
						ProducerId.Meeting.str = str.concat(value).concat(fs);
						return ret;
					}
				}
				
				public static ProducerId ProducerId (String value) {
					ProducerId ret = new ProducerId();
					MaterialflowId.ProducerId.str = str.concat(value).concat(fs);
					return ret;
				}
			}
			
			public static MaterialflowId MaterialflowId (String value) {
				MaterialflowId ret = new MaterialflowId();
				MaterialflowId.str = str.concat(value).concat(fs);
				return ret;
			}
		}
		
		public static class Dev extends RosettaInstance {
			public static final String str = MetsSIPs.str.concat("dev").concat(fs);
		}
		
		public static class Test extends RosettaInstance {
			public static final String str = MetsSIPs.str.concat("test").concat(fs);
		}
		
		public static class Prod extends RosettaInstance{
			public static final String str = MetsSIPs.str.concat("prod").concat(fs);
		}
	}
	
	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		System.out.println(Drive_draft.MetsSIPs.Dev.str);
		System.out.println(Drive_draft.MetsSIPs.Dev.MaterialflowId("1").str);
		System.out.println(Drive_draft.MetsSIPs.Dev.MaterialflowId("2").str);
		System.out.println(Drive_draft.MetsSIPs.Dev.MaterialflowId("1").ProducerId("t").str);
		System.out.println(Drive_draft.MetsSIPs.Dev.MaterialflowId("1").ProducerId("t").Abstract("est").str);
		System.out.println(Drive_draft.MetsSIPs.Dev.MaterialflowId("1").ProducerId("t").Meeting("oast").str);
		System.out.println(Drive_draft.MetsSIPs.Dev.MaterialflowId("1").ProducerId("t").Abstract("est").Mets().str);
//		System.out.println(Drive_draft.)
	}
}
