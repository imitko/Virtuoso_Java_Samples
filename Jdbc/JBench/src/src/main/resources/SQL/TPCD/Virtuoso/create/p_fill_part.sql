--  
--  This file is part of the OpenLink Software Virtuoso Open-Source (VOS)
--  project.
--  
--  Copyright (C) 1998-2021 OpenLink Software
--  
--  This project is free software; you can redistribute it and/or modify it
--  under the terms of the GNU General Public License as published by the
--  Free Software Foundation; only version 2 of the License, dated June 1991.
--  
--  This program is distributed in the hope that it will be useful, but
--  WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
--  General Public License for more details.
--  
--  You should have received a copy of the GNU General Public License along
--  with this program; if not, write to the Free Software Foundation, Inc.,
--  51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
--  
--  
create procedure tpcd..fill_part (in nStartingRow integer, in NumRows integer) {
	
	declare _p_partkey, _p_size integer;
	declare _p_name, _p_mfgr, _p_brand, _p_type, _p_container, _p_comment varchar;
	declare _p_retailprice numeric(20, 2);
	
	declare words, nMfgr, nWord integer;
	
	words := vector('almond', 'antique', 'aquamarine', 'azure', 'beige', 'bisque', 'black', 'blanched', 
			'blue', 'blush', 'brown', 'burlywood', 'burnished', 'chartreuse', 'chiffon', 'chocolate', 
			'coral', 'cornflower', 'cornsilk', 'cream', 'cyan', 'dark', 'deep', 'dim', 'dodger', 'drab', 
			'firebrick', 'floral', 'forest', 'frosted', 'gainsboro', 'ghost', 'goldenrod', 'green', 'grey', 
			'honeydew', 'hot', 'indian', 'ivory', 'khaki', 'lace', 'lavender', 'lawn', 'lemon', 'light', 
			'lime', 'linen', 'magenta', 'maroon', 'medium', 'metallic', 'midnight', 'mint', 'misty', 
			'moccasin', 'navajo', 'navy', 'olive', 'orange', 'orchid', 'pale', 'papaya', 'peach', 'peru', 
			'pink', 'plum', 'powder', 'puff', 'purple', 'red', 'rose', 'rosy', 'royal', 'saddle', 'salmon', 
			'sandy', 'seashell', 'sienna', 'sky', 'slate', 'smoke', 'snow', 'spring', 'steel', 'tan', 
			'thistle', 'tomato', 'turquoise', 'violet', 'wheat', 'white', 'yellow');
	
	_p_partkey := nStartingRow;
	while (_p_partkey <= NumRows) {
		nWord := 0;
		_p_name := '';
		while (nWord < 5) {
			_p_name := concat(_p_name, aref(words, tpcd..randomNumber(0, length(words) - 1)), ' ');
			nWord := nWord + 1;
		}

		nMfgr := tpcd..randomNumber(1, 5);
		_p_mfgr := concat('Manifacturer', sprintf('%d', nMfgr));
		_p_brand := concat('Brand', sprintf('%d%d', tpcd..randomNumber(1, 5), nMfgr));
		_p_type := tpcd..randomType(0);
		_p_size := tpcd..randomNumber(1, 50);
		_p_container := tpcd..randomContainer(0);
		_p_retailprice := (90000 + mod(_p_partkey/10, 20001) + 100 * mod(_p_partkey, 1000))/100;
		_p_comment := tpcd..randomString(14, 0, 1);
		
		insert into tpcd..part (p_partkey, p_name, p_mfgr, p_brand, p_type, p_size, p_container, p_retailprice, p_comment) values (_p_partkey, _p_name, _p_mfgr, _p_brand, _p_type, _p_size, _p_container, _p_retailprice, _p_comment);
		_p_partkey := _p_partkey + 1;
	}
}
