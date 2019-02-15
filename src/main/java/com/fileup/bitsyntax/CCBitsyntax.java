package com.fileup.bitsyntax;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.fileup.bitsyntax.bean.Bitem;
import com.fileup.bitsyntax.bean.Bitem.BitemType;
import com.fileup.bitsyntax.function.FromBytes;
import com.fileup.bitsyntax.function.ToBytes;


/**
 * © 2015-2017 Chenxj Copyright
 *  类 名：CCBitsyntax
 *  类 描 述：Bit syntax
 *  作 者：chenxj
 *  邮 箱：chenios@foxmail.com
 *  日 期：2017年9月15日-下午6:03:15
 */
public class CCBitsyntax extends CCFLib {
	private Random random;
	
	// 分隔符列表,
	private List<byte[]> sps;
	private Map<String, Bitem> allItems;
	private Bitem[] bitems;

	public CCBitsyntax(String des) {
		this.random=new Random();
		this.sps = new ArrayList<>();
		this.allItems = new LinkedHashMap<>();
		this.bitems = this.parser(des);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("sps:\n");
		for (byte[] b : this.sps) {
			sb.append(Arrays.toString(b)).append('\n');
		}
		sb.append("bitems:\n");
		for (Bitem b : bitems) {
			sb.append(b.toString()).append('\n');
		}
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	private <T> T stringToInt(String str) {
		try {
			return (T) Integer.valueOf(str);
		} catch (Exception e) {
			return (T) str;
		}
	}

	private Bitem[] parser(String byntax) {
		StringBuilder sb = new StringBuilder();
		List<Bitem> rl = new ArrayList<>();
		int inArea = 0;
		for (int i = 0; i < byntax.length(); i++) {
			switch (byntax.charAt(i)) {
			case ',':
				if (inArea != 0) {
					sb.append(byntax.charAt(i));
				} else {
					rl.add(this.analyze(sb.toString()));
					sb.delete(0, sb.length());
				}
				break;
			case '"':
			case '\'':
				inArea = -inArea;
				sb.append(byntax.charAt(i));
				break;
			case '(':
			case '{':
			case '[':
			case '<':
				inArea++;
				sb.append(byntax.charAt(i));
				break;
			case ')':
			case '}':
			case ']':
			case '>':
				inArea--;
				sb.append(byntax.charAt(i));
				break;
			case '\\':
				sb.append(byntax.charAt(i++));
				break;
			default:
				sb.append(byntax.charAt(i));
				break;
			}
		}
		rl.add(this.analyze(sb.toString()));
		sb = null;
		Bitem[] r = new Bitem[rl.size()];
		rl.toArray(r);
		return r;
	}

	private Bitem analyze(String el) {
		Bitem rm = new Bitem();
		rm.setSeparatorDataIndex(this.sps.size());
		if ((el.charAt(0) == '\'' && el.charAt(el.length() - 1) == '\'')
				|| (el.charAt(0) == '"' && el.charAt(el.length() - 1) == '"')) {
			rm.setType(BitemType.SEPARATOR);
			// 实际中应该使用stringToUint8Array
			rm.setSeparatorData(el.substring(1, el.length() - 1).getBytes());
			rm.setL(rm.getSeparatorData().length);
			rm.setName(Integer.toHexString(random.nextInt()));
			rm.setNamed(false);
			this.sps.add(rm.getSeparatorData());
			this.allItems.put(rm.getName(), rm);
			return rm;
		} else if (el.charAt(0) == '<' && el.charAt(el.length() - 1) == '>') {
			rm.setType(BitemType.MIXED);
			rm.setName(Integer.toHexString(random.nextInt()));
			rm.setNamed(false);
			this.parserMixed(el, rm);
			this.allItems.put(rm.getName(), rm);
			return rm;
		} else if (el.charAt(0) == '(' && el.charAt(el.length() - 1) == ')') {
			rm.setType(BitemType.RANGE);
			rm.setName(Integer.toHexString(random.nextInt()));
			rm.setNamed(false);
			rm.setBitems(this.parserRange(el.substring(1, el.length() - 1)));
			List<byte[]> range = new ArrayList<>();
			List<Bitem> rg = new ArrayList<>();
			for (int i = 0; i < rm.getBitems().length; i++) {
				if (rm.getBitems()[i].getType() != BitemType.SEPARATOR) {
					rg.add(rm.getBitems()[i]);
				} else {
					range.add(rm.getBitems()[i].getSeparatorData());
				}
			}
			Bitem[] rgg = new Bitem[rg.size()];
			rg.toArray(rgg);
			rm.setRg(rgg);

			byte[][] rag = new byte[range.size()][];
			range.toArray(rag);
			rm.setRange(rag);
			this.allItems.put(rm.getName(), rm);
			return rm;
		}
		StringBuilder sb = new StringBuilder();
		char lastChar = 0;
		int inArea = 0;
		for (int i = 0; i < el.length(); i++) {
			switch (el.charAt(i)) {
			case ':':
				if (inArea == 0) {
					rm.setName(sb.toString());
					sb.delete(0, sb.length());
					lastChar = el.charAt(i);
				} else {
					sb.append(el.charAt(i));
				}
				break;
			case '/':
				if (inArea == 0) {
					if (rm.getName() != null) {
						rm.setL(this.stringToInt(sb.toString()));
					} else {
						rm.setName(sb.toString());
					}
					sb.delete(0, sb.length());
					lastChar = el.charAt(i);
				} else {
					sb.append(el.charAt(i));
				}
				break;
			case '-':
				if (inArea == 0) {
					if (sb.charAt(0) == '[' && sb.charAt(sb.length() - 1) == ']') {
						//
					} else if (sb.charAt(0) == '{' && sb.charAt(sb.length() - 1) == '}') {
						//
					} else if (sb.charAt(0) == '<' && sb.charAt(sb.length() - 1) == '>') {
						//
					} else if (sb.charAt(0) == '(' && sb.charAt(sb.length() - 1) == ')') {
						rm.setBitems(this.parserRange(sb.substring(1, sb.length() - 1)));
						List<byte[]> range = new ArrayList<>();
						for (int j = 0; j < rm.getBitems().length; j++) {
							range.add(rm.getBitems()[j].getSeparatorData());
						}
						byte[][] rag = new byte[range.size()][];
						range.toArray(rag);
						rm.setRange(rag);
					} else {
						rm.getTypes().add(sb.toString());
					}
					sb.delete(0, sb.length());
				} else {
					sb.append(el.charAt(i));
				}
				break;
			case '(':
			case '{':
			case '[':
			case '<':
				inArea++;
				sb.append(el.charAt(i));
				break;
			case ')':
			case '}':
			case ']':
			case '>':
				inArea--;
				sb.append(el.charAt(i));
				break;
			case '\\':
				sb.append(el.charAt(i++));
				break;
			default:
				sb.append(el.charAt(i));
				break;
			}
		}
		if (lastChar == '/') {
			if (sb.charAt(0) == '[' && sb.charAt(sb.length() - 1) == ']') {
				rm.setType(BitemType.LIST);
				rm.setBitems(this.parser(sb.substring(1, sb.length() - 1)));
			} else if (sb.charAt(0) == '{' && sb.charAt(sb.length() - 1) == '}') {
				rm.setType(BitemType.BITMAP);
				rm.setBitems(this.parser(sb.substring(1, sb.length() - 1)));
			} else if (sb.charAt(0) == '<' && sb.charAt(sb.length() - 1) == '>') {
				rm.setType(BitemType.MIXED);
				this.parserMixed(sb.toString(), rm);
			} else if (sb.charAt(0) == '(' && sb.charAt(sb.length() - 1) == ')') {
				rm.setBitems(this.parserRange(sb.substring(1, sb.length() - 1)));
				List<byte[]> range = new ArrayList<>();
				for (int j = 0; j < rm.getBitems().length; j++) {
					range.add(rm.getBitems()[j].getSeparatorData());
				}
				byte[][] rag = new byte[range.size()][];
				range.toArray(rag);
				rm.setRange(rag);
			} else {
				rm.getTypes().add(sb.toString());
			}
		} else if (lastChar == ':') {
			rm.setL(this.stringToInt(sb.toString()));
		} else {
			if (sb.charAt(0) == '(' && sb.charAt(sb.length() - 1) == '}') {
				rm.setType(BitemType.EXCHANGE);
				rm.setExchange(this.parserExchange(sb.substring(1, sb.length() - 1)));
			} else {
				rm.setName(sb.toString());
			}
		}
		sb.delete(0, sb.length());
		if (rm.getName() == null||"".equals(rm.getName())) {
			rm.setName(Integer.toHexString(random.nextInt()));
			rm.setNamed(false);
		} else if ("_".equals(rm.getName())) {
			rm.setType(BitemType.PLACEHOLDER);
		}
		this.allItems.put(rm.getName(), rm);
		return rm;
	}

	private Bitem[] parserRange(String el) {
		StringBuilder sb = new StringBuilder();
		int inArea = 0;
		List<Bitem> rl = new ArrayList<>();
		for (int i = 0; i < el.length(); i++) {
			switch (el.charAt(i)) {
			case '|':
				if (inArea != 0) {
					sb.append(el.charAt(i));
				} else {
					rl.add(this.analyze(sb.toString()));
					sb.delete(0, sb.length());
				}
				break;
			case '"':
			case '\'':
				inArea = -inArea;
				sb.append(el.charAt(i));
				break;
			case '(':
			case '{':
			case '[':
			case '<':
				inArea++;
				sb.append(el.charAt(i));
				break;
			case ')':
			case '}':
			case ']':
			case '>':
				inArea--;
				sb.append(el.charAt(i));
				break;
			case '\\':
				sb.append(el.charAt(i++));
				break;
			default:
				sb.append(el.charAt(i));
				break;
			}
		}
		rl.add(this.analyze(sb.toString()));
		sb.delete(0, sb.length());
		Bitem[] r = new Bitem[rl.size()];
		rl.toArray(r);
		return r;
	}

	private void parserMixed(String el, Bitem rm) {
		if (el.charAt(1) == '?') {
			rm.setDispensable(true);
			el = el.substring(2, el.length() - 2);
		} else {
			rm.setDispensable(false);
			el = el.substring(1, el.length() - 1);
		}
		rm.setBitems(this.parser(el));
	}

	private Object[] parserExchange(String el) {
		List<Object> rl = new ArrayList<>();
		int index = el.indexOf("){");
		rl.add(el.substring(0, index));
		el = el.substring(index + 2, el.length());

		int inArea = 0;
		StringBuilder sb = new StringBuilder();
		List<Object> exs = new ArrayList<>();
		for (int i = 0; i < el.length(); i++) {
			switch (el.charAt(i)) {
			case ':':
				if (inArea == 0) {
					exs.add(this.stringToInt(sb.toString()));
					sb.delete(0, sb.length());
				} else {
					sb.append(el.charAt(i));
				}
				break;
			case ',':
				if (inArea == 0) {
					exs.add(this.parser(sb.substring(1, sb.length() - 1)));
					sb.delete(0, sb.length());
				} else {
					sb.append(el.charAt(i));
				}
				break;
			case '"':
			case '\'':
				inArea = -inArea;
				sb.append(el.charAt(i));
				break;
			case '(':
			case '{':
			case '[':
			case '<':
				inArea++;
				sb.append(el.charAt(i));
				break;
			case ')':
			case '}':
			case ']':
			case '>':
				inArea--;
				sb.append(el.charAt(i));
				break;
			case '\\':
				sb.append(el.charAt(i++));
				break;
			default:
				sb.append(el.charAt(i));
				break;
			}
		}
		exs.add(this.parser(sb.substring(1, sb.length() - 1)));
		sb.delete(0, sb.length());
		rl.add(exs.toArray());
		return rl.toArray();
	}

	public Map<String, Object> convertToMap(Object a, Object des) {
		Map<String, Object> rs = new HashMap<>();
		CCDataView dv;
		if (a instanceof CCDataView) {
			dv = (CCDataView) a;
		} else if (a instanceof ByteBuffer) {
			dv = new CCDataView((ByteBuffer) a);
		} else {
			dv = new CCDataView((byte[]) a);
		}
		Bitem[] bitems;
		if (des != null) {
			if (des instanceof String) {
				bitems = this.parser((String) des);
			} else {
				bitems = (Bitem[]) des;
			}
		} else {
			bitems = this.bitems;
		}
		int[] r = this.doConvertTo(bitems, dv, rs, false);
		if (r[0] != 0) {
			for (int i = bitems.length - 1; i >= r[1]; i--) {
				this.doConvert(bitems[i], dv, rs, true);
			}
		}
		return rs;
	}
	public Map<String, Object> convertToMap(Object a) {
		return convertToMap(a,null);
	}

	private int[] doConvertTo(Bitem[] bitems, CCDataView dv, Map<String, Object> rs, boolean revert) {
		if (revert) {
			for (int i = bitems.length - 1; i > -1; i--) {
				int[] r = this.doConvert(bitems[i], dv, rs, revert);
				if (r[0] != 0) {
					return new int[] { r[0], i };
				}
			}
		} else {
			for (int i = 0; i < bitems.length; i++) {
				int[] r = this.doConvert(bitems[i], dv, rs, revert);
				if (r[0] != 0) {
					return new int[] { r[0], i };
				}
			}
		}
		return new int[] { 0 };
	}

	private int[] doConvert(Bitem bitem, CCDataView dv, Map<String, Object> rs, boolean revert) {
		if (bitem.getL() != null) {
			switch (bitem.getType()) {
			case SEPARATOR:
				if (revert) {
					dv.limit(dv.limit() - (int) bitem.getL());
				} else {
					dv.position(dv.position() + (int) bitem.getL());
				}
				break;
			case LIST:
				List<Object> datas = new ArrayList<>();
				boolean itemIsArray = false;
				if (bitem.getBitems().length == 1) {
					itemIsArray = !bitem.getBitems()[0].isNamed();
				}
				if (bitem.getL() instanceof String) {
					if (itemIsArray) {
						for (int j = 0; j < (int) rs.get(bitem.getL()); j++) {
							datas.add(dv.getArray(bitem.getBitems()[0].getL()));
						}
					} else {
						for (int j = 0; j < (int) rs.get(bitem.getL()); j++) {
							datas.add(this.convertToMap(dv, bitem.getBitems()));
						}
					}
				} else {
					if (itemIsArray) {
						for (int j = 0; j < (int) bitem.getL(); j++) {
							datas.add(dv.getArray(bitem.getBitems()[0].getL()));
						}
					} else {
						for (int j = 0; j < (int) bitem.getL(); j++) {
							datas.add(this.convertToMap(dv, bitem.getBitems()));
						}
					}
				}
				rs.put(bitem.getName(), datas);
				break;
			case BITMAP:
				CCBitmap bm;
				if (bitem.getL() instanceof String) {
					bm = new CCBitmap(bitem.getBitems(), dv.getArray(bitem.getL(), revert));
				} else {
					bm = new CCBitmap(bitem.getBitems(), dv.getArray(rs.get(bitem.getL()), revert));
				}
				Bitem[] bms = bm.getBm();
				Map<String, Object> bmv = bm.toMap();
				for (int j = 0; j < bms.length; j++) {
					if (!"_".equals(bms[j].getName())) {
						bmv.put(bms[j].getName(),
								convertByTypes(fromLib, bms[j].getTypes(), bmv.get(bms[j].getName()), -1, false));
					}
				}
				if (bitem.isNamed()) {
					rs.put(bitem.getName(), bmv);
				} else {
					rs.putAll(bmv);
				}
				break;
			case PLACEHOLDER: // 只适合读取用来跳过数据读取
				if (bitem.getL() instanceof String) {
					dv.position(dv.position() + (int) rs.get(bitem.getL()));
				} else {
					dv.position(dv.position() + (int) bitem.getL());
				}
				break;
			default:
				Object v;
				if (bitem.getL() instanceof String) {
					v = dv.getArray((int) rs.get(bitem.getL()), revert);
				} else {
					v = dv.getArray(bitem.getL(), revert);
				}
				if (!bitem.getTypes().isEmpty()) {
					int l = bitem.getTypes().size();
					for (int j = 0; j < l; j++) {
						v = fromLib.get(bitem.getTypes().get(j)).apply(v);
					}
				}
				rs.put(bitem.getName(), v);
				break;
			}
		} else {
			switch (bitem.getType()) {
			case EXCHANGE:
				Object[] exc = bitem.getExchange();
				int[] r = new int[] { 0 };
				if (exc[0] instanceof String) {
					Object refCondition = rs.get(exc[0]);
					int convertFlag = -1;
					Object[] exc_1 = (Object[]) exc[1];
					if(refCondition instanceof String){
						for (int j = 0; j < exc_1.length; j++) {
							if (refCondition.equals(exc_1[j])) {
								r = this.doConvertTo((Bitem[]) exc_1[++j], dv, rs, revert);
								convertFlag--;
								break;
							} else if ("".equals(exc_1[j])) {
								convertFlag = ++j;
							}else {
								j++;
							}
						}
					}else{
						for (int j = 0; j < exc_1.length; j++) {
							if ((int)refCondition==(int)exc_1[j]) {
								r = this.doConvertTo((Bitem[]) exc_1[++j], dv, rs, revert);
								convertFlag--;
								break;
							} else if ("".equals(exc_1[j])) {
								convertFlag = ++j;
							}else{
								j++;
							}
						}
					}
					if (convertFlag > -1) {
						r = this.doConvertTo((Bitem[]) exc_1[convertFlag], dv, rs, revert);
					}
				}
				return r;
			case MIXED:
				switch (bitem.getBitems()[0].getType()) {
				case SEPARATOR:
					Object[] matchsResult = dv.matchsAt(dv.position(),
							new byte[][] { bitem.getBitems()[0].getSeparatorData() });
					if ((boolean) matchsResult[0]) {
						if (bitem.isNamed()) {
							Map<String, Object> oo = new HashMap<>();
							int[] rr = this.doConvertTo(bitem.getBitems(), dv, oo, revert);
							rs.put(bitem.getName(), oo);
							return rr;
						} else {
							return this.doConvertTo(bitem.getBitems(), dv, rs, revert);
						}
					}
					break;
				default:
					if (bitem.getBitems()[0].getRange().length != 0) {
						Object[] matchsResult2 = dv.matchsAt(dv.position(), bitem.getBitems()[0].getRange());
						if ((boolean) matchsResult2[0]) {
							if (bitem.isNamed()) {
								Map<String, Object> oo = new HashMap<>();
								int[] rr = this.doConvertTo(bitem.getBitems(), dv, oo, revert);
								rs.put(bitem.getName(), oo);
								return rr;
							} else {
								return this.doConvertTo(bitem.getBitems(), dv, rs, revert);
							}
						}
					} else if (!bitem.isDispensable()) {
						if (bitem.isNamed()) {
							Map<String, Object> oo = new HashMap<>();
							int[] rr = this.doConvertTo(bitem.getBitems(), dv, oo, revert);
							rs.put(bitem.getName(), oo);
							return rr;
						} else {
							return this.doConvertTo(bitem.getBitems(), dv, rs, revert);
						}
					}
					;
					break;
				}
				break;
			case RANGE:
				Object[] matchsResult = dv.matchsAt(dv.position(), bitem.getRange());
				if (!(boolean) matchsResult[0] && bitem.getRg().length != 0) {
					return this.doConvertTo(bitem.getRg(), dv, rs, revert);
				} else {
					dv.position(dv.position() + ((Object[]) matchsResult[1]).length);
				}
				break;
			default:
				if (bitem.getRange()!=null) {
					Object[] matchsResult2 = dv.matchsAt(dv.position(), bitem.getRange());
					if ((boolean) matchsResult2[0]) {
						if (revert) {
							dv.limit(dv.limit() - ((byte[]) matchsResult2[1]).length);
						} else {
							dv.position(dv.position() + ((byte[]) matchsResult2[1]).length);
						}
						Object v = matchsResult2[1];
						if (!bitem.getTypes().isEmpty()) {
							int l = bitem.getTypes().size();
							for (int j = 0; j < l; j++) {
								v = fromLib.get(bitem.getTypes().get(j)).apply(v);
							}
						}
						rs.put(bitem.getName(), v);
					}
				} else {
					List<byte[]> lsb = this.sps.subList(bitem.getSeparatorDataIndex(), sps.size());
					byte[][] separatorDatas = new byte[lsb.size()][];
					lsb.toArray(separatorDatas);

					if (separatorDatas.length != 0) {
						if (bitem.getType() != BitemType.PLACEHOLDER) {
							Object v = dv.getArray(separatorDatas, revert);
							if (!bitem.getTypes().isEmpty()) {
								int l = bitem.getTypes().size();
								for (int j = 0; j < l; j++) {
									v = fromLib.get(bitem.getTypes().get(j)).apply(v);
								}
							}
							rs.put(bitem.getName(), v);
						} else {
							if (revert) {
								dv.limit(dv.limit() - dv.arrayMatch(separatorDatas, revert));
							} else {
								dv.position(dv.position() + dv.arrayMatch(separatorDatas, revert));
							}
						}
					} else {
						if (revert) {
							if (bitem.getType() != BitemType.PLACEHOLDER) {
								Object v = dv.getArray(null, revert);
								if (!bitem.getTypes().isEmpty()) {
									int l = bitem.getTypes().size();
									for (int j = 0; j < l; j++) {
										v = fromLib.get(bitem.getTypes().get(j)).apply(v);
									}
								}
								rs.put(bitem.getName(), v);
							} else {
								dv.limit(dv.position());
							}
						}
						return new int[] { 1 };
					}
				}
				break;
			}
		}
		return new int[] { 0 };
	}

	@SuppressWarnings("unchecked")
	private Map<String, byte[]> convertToBytesMap(Map<String, Object> o, Object des) {
		Map<String, byte[]> rm = new LinkedHashMap<>();
		Bitem[] bitems;
		if (des != null) {
			if (des instanceof String) {
				bitems = this.parser((String) des);
			} else {
				bitems = (Bitem[]) des;
			}
		} else {
			bitems = this.bitems;
		}
		for (int i = 0; i < bitems.length; i++) {
			Bitem bitem = bitems[i];
			switch (bitem.getType()) {
			case SEPARATOR:
				rm.put(bitem.getName(), bitem.getSeparatorData());
				break;
			case LIST:
				Object[] datas;
				Object dts = o.get(bitem.getName());
				if (dts instanceof List) {
					datas = ((List<?>) dts).toArray();
				} else {
					datas = (Object[]) dts;
				}
				CCBytes da = new CCBytes();
				boolean itemIsArray = false;
				if (bitem.getBitems().length == 1) {
					itemIsArray = !bitem.getBitems()[0].isNamed();
				}
				if (bitem.getL() instanceof String) {
					if (itemIsArray) {
						for (int j = 0; j < datas.length; j++) {
							da.addAll((byte[]) datas[j]);
						}
					} else {
						for (int j = 0; j < datas.length; j++) {
							da.addAll(this.convertToByteArray((Map<String, Object>) datas[j], bitem.getBitems()));
						}
					}
					Bitem refBitem = this.allItems.get(bitem.getL());
					Object tv = datas.length;
					int typesl = refBitem.getTypes().size();
					for (int j = typesl - 1; j > -1; j--) {
						tv = toLib.get(refBitem.getTypes().get(j)).apply(tv, refBitem.getL());
					}
					rm.put(bitem.getL(), (byte[])tv);
					// 别的地方可能需要使用
					o.put(bitem.getL(), datas.length);

				} else {
					if (itemIsArray) {
						for (int j = 0; j < (int) bitem.getL(); j++) {
							da.addAll((byte[]) datas[j]);
						}
					} else {
						for (int j = 0; j < (int) bitem.getL(); j++) {
							da.addAll(this.convertToByteArray((Map<String, Object>) datas[j], bitem.getBitems()));
						}
					}
				}
				rm.put(bitem.getName(), da.getBytes());
				break;
			case EXCHANGE:
				Object[] exc = bitem.getExchange();
				if (exc[0] instanceof String) {
					Object refCondition =o.get(exc[0]);
					Object[] exc_1 = (Object[]) exc[1];
					int convertFlag = -1;
					if(refCondition instanceof String){
						for (int j = 0; j < exc_1.length; j++) {
							if (refCondition.equals(exc_1[j])) {
								rm.put(bitem.getName(), this.convertToByteArray(o, exc_1[++j]));
								convertFlag--;
								break;
							} else if ("".equals(exc_1[j])) {
								convertFlag = ++j;
							}else{
								j++;
							}
						}
					}else{
						for (int j = 0; j < exc_1.length; j++) {
							if ((int)refCondition==(int)exc_1[j]) {
								rm.put(bitem.getName(), this.convertToByteArray(o, exc_1[++j]));
								convertFlag--;
								break;
							} else if ("".equals(exc_1[j])) {
								convertFlag = ++j;
							}else{
								j++;
							}
						}
					}
					if (convertFlag > -1) {
						rm.put(bitem.getName(), this.convertToByteArray(o, exc_1[convertFlag]));
					}
				}
				break;
			case BITMAP:
				Map<String, Object> valueMap = (Map<String, Object>) o.get(bitem.getName());

				CCBitmap bitmap = new CCBitmap(bitem.getBitems(), null);
				Bitem[] bms = bitmap.getBm();
				for (int j = 0; j < bms.length; j++) {
					bitmap.setItemValue(bms[j].getName(), convertByTypes(toLib, bms[j].getTypes(),
							valueMap.get(bms[j].getName()), bms[j].getL(), true));
				}
				byte[] bs = bitmap.buffer();
				rm.put(bitem.getName(), bs);
				if (bitem.getL() instanceof String) {

					Bitem refBitem = this.allItems.get(bitem.getL());
					Object tv = bs.length;
					int typesl = refBitem.getTypes().size();
					for (int j = typesl - 1; j > -1; j--) {
						tv = toLib.get(refBitem.getTypes().get(j)).apply(tv, refBitem.getL());
					}
					rm.put(bitem.getL(), (byte[])tv);
					o.put(bitem.getL(), bs.length);
				}
				break;
			case MIXED:
				if (bitem.isNamed()) {
					if (o.containsKey(bitem.getName())) {
						rm.put(bitem.getName(), this.convertToByteArray((Map<String, Object>) o.get(bitem.getName()),
								bitem.getBitems()));
					}
				} else {
					for (int mi = 0; mi < bitem.getBitems().length; mi++) {
						if (o.containsKey(bitem.getBitems()[mi].getName())) {
							rm.put(bitem.getName(), this.convertToByteArray(o, bitem.getBitems()));
							break;
						}
					}
				}
				if (!bitem.isDispensable()) {
					// TODO
				}
				break;
			case RANGE:
				if (bitem.getRg().length > 0 && o.containsKey(bitem.getRg()[0].getName())) {
					Object tv = o.get(bitem.getRg()[0].getName());
					int typesl = bitem.getRg()[0].getTypes().size();
					for (int j = typesl - 1; j > -1; j--) {
						tv = toLib.get(bitem.getRg()[0].getTypes().get(j)).apply(tv, -1);
					}
					rm.put(bitem.getName(), (byte[])tv);
				} else {
					rm.put(bitem.getName(), bitem.getRange()[0]);
				}
				break;
			default:
				if (bitem.getL() instanceof String) {
					Object tv = o.get(bitem.getName());
					int typesl = bitem.getTypes().size();
					for (int j = typesl - 1; j > -1; j--) {
						tv = toLib.get(bitem.getTypes().get(j)).apply(tv, -1);
					}
					rm.put(bitem.getName(), (byte[])tv);
					o.put(bitem.getL(), ((byte[]) tv).length);
					
					Bitem refBitem = this.allItems.get(bitem.getL());
					tv = ((byte[]) tv).length;
					typesl = refBitem.getTypes().size();
					for (int j = typesl - 1; j > -1; j--) {
						tv = toLib.get(refBitem.getTypes().get(j)).apply(tv, refBitem.getL());
					}
					rm.put(bitem.getL(), (byte[])tv);
				} else {
					Object tv = o.get(bitem.getName());
					int typesl = bitem.getTypes().size();
					for (int j = typesl - 1; j > -1; j--) {
						tv = toLib.get(bitem.getTypes().get(j)).apply(tv, bitem.getL());
					}
					rm.put(bitem.getName(), (byte[])tv);
				}
				break;
			}
		}
		return rm;
	}
	public byte[] convertToByteArray(Map<String, Object> o, Object des){
		Map<String, byte[]> rm=this.convertToBytesMap(o, des);
		CCBytes rs = new CCBytes();
		for (Entry<String, byte[]> entry : rm.entrySet()) {
			rs.addAll((byte[]) entry.getValue());
		}
		return rs.getBytes();
	}
	public byte[] convertToByteArray(Map<String, Object> o){
		return convertToByteArray(o, null);
	}
	public void writeToOutPutStream(Map<String, Object> o, Object des,OutputStream out){
		Map<String, byte[]> rm=this.convertToBytesMap(o, des);
		try{
			for (Entry<String, byte[]> entry : rm.entrySet()) {
				out.write((byte[]) entry.getValue());
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void writeToOutPutStream(Map<String, Object> o,OutputStream out){
		writeToOutPutStream(o, null, out);
	}
	@SuppressWarnings("unchecked")
	private <T> T convertByTypes(Map<String, ?> lib, List<String> types, Object value, int length, boolean d) {
		int l = types.size();
		if (!d) {
			for (int i = 0; i < l; i++) {
				value = ((FromBytes) lib.get(types.get(i))).apply(value);
			}
		} else {
			for (int i = l - 1; i > -1; i--) {
				value = ((ToBytes) lib.get(types.get(i))).apply(value, length);
			}
		}
		return (T) value;
	}
}
