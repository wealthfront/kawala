package com.kaching.platform.common.values;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * Country names and alpha-2 codes as defined by
 * <a href="http://en.wikipedia.org/wiki/ISO_3166-1">ISO 3166-1</a>. This
 * enumeration was generated from the second edition of ISO 3166-1 and was
 * last updated on February, 22 2010 based on the ISO 3166-1 Newsletter VI-7.
 */
public enum Country implements NumberedValue {

  AF(1, "Afghanistan"),
  AX(2, "\u00c5land Islands"),
  AL(3, "Albania"),
  DZ(4, "Algeria"),
  AS(5, "American Samoa"),
  AD(6, "Andorra"),
  AO(7, "Angola"),
  AI(8, "Anguilla"),
  AQ(9, "Antarctica"),
  AG(10, "Antigua and Barbuda"),
  AR(11, "Argentina"),
  AM(12, "Armenia"),
  AW(13, "Aruba"),
  AU(14, "Australia"),
  AT(15, "Austria"),
  AZ(16, "Azerbaijan"),
  BS(17, "Bahamas"),
  BH(18, "Bahrain"),
  BD(19, "Bangladesh"),
  BB(20, "Barbados"),
  BY(21, "Belarus"),
  BE(22, "Belgium"),
  BZ(23, "Belize"),
  BJ(24, "Benin"),
  BM(25, "Bermuda"),
  BT(26, "Bhutan"),
  BO(27, "Bolivia, Plurinational State of"),
  BA(28, "Bosnia and Herzegovina"),
  BW(29, "Botswana"),
  BV(30, "Bouvet Island"),
  BR(31, "Brazil"),
  IO(32, "British Indian Ocean Territory"),
  BN(33, "Brunei Darussalam"),
  BG(34, "Bulgaria"),
  BF(35, "Burkina Faso"),
  BI(36, "Burundi"),
  KH(37, "Cambodia"),
  CM(38, "Cameroon"),
  CA(39, "Canada"),
  CV(40, "Cape Verde"),
  KY(41, "Cayman Islands"),
  CF(42, "Central African Republic"),
  TD(43, "Chad"),
  CL(44, "Chile"),
  CN(45, "China"),
  CX(46, "Christmas Island"),
  CC(47, "Cocos (Keeling) Islands"),
  CO(48, "Colombia"),
  KM(49, "Comoros"),
  CG(50, "Congo"),
  CD(51, "Congo, the Democratic Republic of the"),
  CK(52, "Cook Islands"),
  CR(53, "Costa Rica"),
  CI(54, "C\u00f4te d'Ivoire"),
  HR(55, "Croatia"),
  CU(56, "Cuba"),
  CY(57, "Cyprus"),
  CZ(58, "Czech Republic"),
  DK(59, "Denmark"),
  DJ(60, "Djibouti"),
  DM(61, "Dominica"),
  DO(62, "Dominican Republic"),
  EC(63, "Ecuador"),
  EG(64, "Egypt"),
  SV(65, "El Salvador"),
  GQ(66, "Equatorial Guinea"),
  ER(67, "Eritrea"),
  EE(68, "Estonia"),
  ET(69, "Ethiopia"),
  FK(70, "Falkland Islands (Malvinas)"),
  FO(71, "Faroe Islands"),
  FJ(72, "Fiji"),
  FI(73, "Finland"),
  FR(74, "France"),
  GF(75, "French Guiana"),
  PF(76, "French Polynesia"),
  TF(77, "French Southern Territories"),
  GA(78, "Gabon"),
  GM(79, "Gambia"),
  GE(80, "Georgia"),
  DE(81, "Germany"),
  GH(82, "Ghana"),
  GI(83, "Gibraltar"),
  GR(84, "Greece"),
  GL(85, "Greenland"),
  GD(86, "Grenada"),
  GP(87, "Guadeloupe"),
  GU(88, "Guam"),
  GT(89, "Guatemala"),
  GG(90, "Guernsey"),
  GN(91, "Guinea"),
  GW(92, "Guinea-Bissau"),
  GY(93, "Guyana"),
  HT(94, "Haiti"),
  HM(95, "Heard Island and McDonald Islands"),
  VA(96, "Holy See (Vatican City State)"),
  HN(97, "Honduras"),
  HK(98, "Hong Kong"),
  HU(99, "Hungary"),
  IS(100, "Iceland"),
  IN(101, "India"),
  ID(102, "Indonesia"),
  IR(103, "Iran, Islamic Republic of"),
  IQ(104, "Iraq"),
  IE(105, "Ireland"),
  IM(106, "Isle of Man"),
  IL(107, "Israel"),
  IT(108, "Italy"),
  JM(109, "Jamaica"),
  JP(110, "Japan"),
  JE(111, "Jersey"),
  JO(112, "Jordan"),
  KZ(113, "Kazakhstan"),
  KE(114, "Kenya"),
  KI(115, "Kiribati"),
  KP(116, "Korea, Democratic People's Republic of"),
  KR(117, "Korea, Republic of"),
  KW(118, "Kuwait"),
  KG(119, "Kyrgyzstan"),
  LA(120, "Lao People's Democratic Republic"),
  LV(121, "Latvia"),
  LB(122, "Lebanon"),
  LS(123, "Lesotho"),
  LR(124, "Liberia"),
  LY(125, "Libyan Arab Jamahiriya"),
  LI(126, "Liechtenstein"),
  LT(127, "Lithuania"),
  LU(128, "Luxembourg"),
  MO(129, "Macao"),
  MK(130, "Macedonia, the former Yugoslav Republic of"),
  MG(131, "Madagascar"),
  MW(132, "Malawi"),
  MY(133, "Malaysia"),
  MV(134, "Maldives"),
  ML(135, "Mali"),
  MT(136, "Malta"),
  MH(137, "Marshall Islands"),
  MQ(138, "Martinique"),
  MR(139, "Mauritania"),
  MU(140, "Mauritius"),
  YT(141, "Mayotte"),
  MX(142, "Mexico"),
  FM(143, "Micronesia, Federated States of"),
  MD(144, "Moldova, Republic of"),
  MC(145, "Monaco"),
  MN(146, "Mongolia"),
  ME(147, "Montenegro"),
  MS(148, "Montserrat"),
  MA(149, "Morocco"),
  MZ(150, "Mozambique"),
  MM(151, "Myanmar"),
  NA(152, "Namibia"),
  NR(153, "Nauru"),
  NP(154, "Nepal"),
  NL(155, "Netherlands"),
  AN(156, "Netherlands Antilles"),
  NC(157, "New Caledonia"),
  NZ(158, "New Zealand"),
  NI(159, "Nicaragua"),
  NE(160, "Niger"),
  NG(161, "Nigeria"),
  NU(162, "Niue"),
  NF(163, "Norfolk Island"),
  MP(164, "Northern Mariana Islands"),
  NO(165, "Norway"),
  OM(166, "Oman"),
  PK(167, "Pakistan"),
  PW(168, "Palau"),
  PS(169, "Palestinian Territory, Occupied"),
  PA(170, "Panama"),
  PG(171, "Papua New Guinea"),
  PY(172, "Paraguay"),
  PE(173, "Peru"),
  PH(174, "Philippines"),
  PN(175, "Pitcairn"),
  PL(176, "Poland"),
  PT(177, "Portugal"),
  PR(178, "Puerto Rico"),
  QA(179, "Qatar"),
  RE(180, "R\u00e9union"),
  RO(181, "Romania"),
  RU(182, "Russian Federation"),
  RW(183, "Rwanda"),
  BL(184, "Saint Barth\u00e9lemy"),
  SH(185, "Saint Helena, Ascension and Tristan da Cunha"),
  KN(186, "Saint Kitts and Nevis"),
  LC(187, "Saint Lucia"),
  MF(188, "Saint Martin (French part)"),
  PM(189, "Saint Pierre and Miquelon"),
  VC(190, "Saint Vincent and the Grenadines"),
  WS(191, "Samoa"),
  SM(192, "San Marino"),
  ST(193, "Sao Tome and Principe"),
  SA(194, "Saudi Arabia"),
  SN(195, "Senegal"),
  RS(196, "Serbia"),
  SC(197, "Seychelles"),
  SL(198, "Sierra Leone"),
  SG(199, "Singapore"),
  SK(200, "Slovakia"),
  SI(201, "Slovenia"),
  SB(202, "Solomon Islands"),
  SO(203, "Somalia"),
  ZA(204, "South Africa"),
  GS(205, "South Georgia and the South Sandwich Islands"),
  ES(206, "Spain"),
  LK(207, "Sri Lanka"),
  SD(208, "Sudan"),
  SR(209, "Suriname"),
  SJ(210, "Svalbard and Jan Mayen"),
  SZ(211, "Swaziland"),
  SE(212, "Sweden"),
  CH(213, "Switzerland"),
  SY(214, "Syrian Arab Republic"),
  TW(215, "Taiwan, Province of China"),
  TJ(216, "Tajikistan"),
  TZ(217, "Tanzania, United Republic of"),
  TH(218, "Thailand"),
  TL(219, "Timor-Leste"),
  TG(220, "Togo"),
  TK(221, "Tokelau"),
  TO(222, "Tonga"),
  TT(223, "Trinidad and Tobago"),
  TN(224, "Tunisia"),
  TR(225, "Turkey"),
  TM(226, "Turkmenistan"),
  TC(227, "Turks and Caicos Islands"),
  TV(228, "Tuvalu"),
  UG(229, "Uganda"),
  UA(230, "Ukraine"),
  AE(231, "United Arab Emirates"),
  GB(232, "United Kingdom"),
  US(233, "United States"),
  UM(234, "United States Minor Outlying Islands"),
  UY(235, "Uruguay"),
  UZ(236, "Uzbekistan"),
  VU(237, "Vanuatu"),
  VE(238, "Venezuela, Bolivarian Republic of"),
  VN(239, "Viet Nam"),
  VG(240, "Virgin Islands, British"),
  VI(241, "Virgin Islands, U.S."),
  WF(242, "Wallis and Futuna"),
  EH(243, "Western Sahara"),
  YE(244, "Yemen"),
  ZM(245, "Zambia"),
  ZW(246, "Zimbabwe");

  private final int number;
  private final String countryName;

  private Country(int number, String countryName) {
    this.number = number;
    this.countryName = countryName;
  }

  /**
   * Returns a consistently assigned number for the country.
   * @see NumberedValue
   */
  @Override
  public int getNumber() {
    return number;
  }

  /**
   * Returns the country name.
   */
  public String getCountryName() {
    return countryName;
  }

  /**
   * Returns the ISO 3166-1 alpha-2 code assigned to the country.
   */
  public String getAlpha2() {
    return name();
  }

  private static final Map<Integer, Country> map =
      ImmutableMap.<Integer, Country> builder()
          .put(1, AF)
          .put(2, AX)
          .put(3, AL)
          .put(4, DZ)
          .put(5, AD)
          .put(6, AO)
          .put(7, AI)
          .put(8, AQ)
          .put(9, AG)
          .put(10, AR)
          .put(12, AM)
          .put(13, AW)
          .put(14, AU)
          .put(15, AT)
          .put(16, AZ)
          .put(17, BS)
          .put(18, BH)
          .put(19, BD)
          .put(20, BB)
          .put(21, BY)
          .put(22, BE)
          .put(23, BZ)
          .put(24, BJ)
          .put(25, BM)
          .put(26, BT)
          .put(27, BO)
          .put(28, BA)
          .put(29, BW)
          .put(30, BV)
          .put(31, BR)
          .put(32, IO)
          .put(33, BN)
          .put(34, BG)
          .put(35, BF)
          .put(36, BI)
          .put(37, KH)
          .put(38, CM)
          .put(39, CA)
          .put(40, CV)
          .put(41, KY)
          .put(42, CF)
          .put(43, TD)
          .put(44, CL)
          .put(45, CN)
          .put(46, CX)
          .put(47, CC)
          .put(48, CO)
          .put(49, KM)
          .put(50, CG)
          .put(51, CD)
          .put(52, CK)
          .put(53, CR)
          .put(54, CI)
          .put(55, HR)
          .put(56, CU)
          .put(57, CY)
          .put(58, CZ)
          .put(59, DK)
          .put(60, DJ)
          .put(61, DM)
          .put(62, DO)
          .put(63, EC)
          .put(64, EG)
          .put(65, SV)
          .put(66, GQ)
          .put(67, ER)
          .put(68, EE)
          .put(69, ET)
          .put(70, FK)
          .put(71, FO)
          .put(72, FJ)
          .put(73, FI)
          .put(74, FR)
          .put(75, GF)
          .put(76, PF)
          .put(77, TF)
          .put(78, GA)
          .put(79, GM)
          .put(80, GE)
          .put(81, DE)
          .put(82, GH)
          .put(83, GI)
          .put(84, GR)
          .put(85, GL)
          .put(86, GD)
          .put(87, GP)
          .put(88, GU)
          .put(89, GT)
          .put(90, GG)
          .put(91, GN)
          .put(92, GW)
          .put(93, GY)
          .put(94, HT)
          .put(95, HM)
          .put(96, VA)
          .put(97, HN)
          .put(98, HK)
          .put(99, HU)
          .put(100, IS)
          .put(101, IN)
          .put(102, ID)
          .put(103, IR)
          .put(104, IQ)
          .put(105, IE)
          .put(106, IM)
          .put(107, IL)
          .put(108, IT)
          .put(109, JM)
          .put(110, JP)
          .put(111, JE)
          .put(112, JO)
          .put(113, KZ)
          .put(114, KE)
          .put(115, KI)
          .put(116, KP)
          .put(117, KR)
          .put(118, KW)
          .put(119, KG)
          .put(120, LA)
          .put(121, LV)
          .put(122, LB)
          .put(123, LS)
          .put(124, LR)
          .put(125, LY)
          .put(126, LI)
          .put(127, LT)
          .put(128, LU)
          .put(129, MO)
          .put(130, MK)
          .put(131, MG)
          .put(132, MW)
          .put(133, MY)
          .put(134, MV)
          .put(135, ML)
          .put(136, MT)
          .put(137, MH)
          .put(138, MQ)
          .put(139, MR)
          .put(140, MU)
          .put(141, YT)
          .put(142, MX)
          .put(143, FM)
          .put(144, MD)
          .put(145, MC)
          .put(146, MN)
          .put(147, ME)
          .put(148, MS)
          .put(149, MA)
          .put(150, MZ)
          .put(151, MM)
          .put(152, NA)
          .put(153, NR)
          .put(154, NP)
          .put(155, NL)
          .put(156, AN)
          .put(157, NC)
          .put(158, NZ)
          .put(159, NI)
          .put(160, NE)
          .put(161, NG)
          .put(162, NU)
          .put(163, NF)
          .put(164, MP)
          .put(165, NO)
          .put(166, OM)
          .put(167, PK)
          .put(168, PW)
          .put(169, PS)
          .put(170, PA)
          .put(171, PG)
          .put(172, PY)
          .put(173, PE)
          .put(174, PH)
          .put(175, PN)
          .put(176, PL)
          .put(177, PT)
          .put(178, PR)
          .put(179, QA)
          .put(180, RE)
          .put(181, RO)
          .put(182, RU)
          .put(183, RW)
          .put(184, BL)
          .put(185, SH)
          .put(186, KN)
          .put(187, LC)
          .put(188, MF)
          .put(189, PM)
          .put(190, VC)
          .put(191, WS)
          .put(192, SM)
          .put(193, ST)
          .put(194, SA)
          .put(195, SN)
          .put(196, RS)
          .put(197, SC)
          .put(198, SL)
          .put(199, SG)
          .put(200, SK)
          .put(201, SI)
          .put(202, SB)
          .put(203, SO)
          .put(204, ZA)
          .put(205, GS)
          .put(206, ES)
          .put(207, LK)
          .put(208, SD)
          .put(209, SR)
          .put(210, SJ)
          .put(211, SZ)
          .put(212, SE)
          .put(213, CH)
          .put(214, SY)
          .put(215, TW)
          .put(216, TJ)
          .put(217, TZ)
          .put(218, TH)
          .put(219, TL)
          .put(220, TG)
          .put(221, TK)
          .put(222, TO)
          .put(223, TT)
          .put(224, TN)
          .put(225, TR)
          .put(226, TM)
          .put(227, TC)
          .put(228, TV)
          .put(229, UG)
          .put(230, UA)
          .put(231, AE)
          .put(232, GB)
          .put(233, US)
          .put(234, UM)
          .put(235, UY)
          .put(236, UZ)
          .put(237, VU)
          .put(238, VE)
          .put(239, VN)
          .put(240, VG)
          .put(241, VI)
          .put(242, WF)
          .put(243, EH)
          .put(244, YE)
          .put(245, ZM)
          .put(246, ZW)
          .build();

  public static Country fromNumber(int number) {
    return map.get(number);
  }

}
